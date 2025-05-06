package io.hynix.units.impl.combat;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.EventInput;
import io.hynix.events.impl.EventMotion;
import io.hynix.events.impl.EventUpdate;
import io.hynix.events.impl.EventPlaceObsidian;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeListSetting;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.InventoryUtils;
import io.hynix.utils.player.MoveUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;
import java.util.stream.Collectors;

@UnitRegister(name = "AutoCrystal",
        category = Category.Combat,
        desc = "Сам взрывает кристалл, когда ставишь обсидиан")
public class AutoCrystal extends Unit {

    public final ModeListSetting options = new ModeListSetting("Опции",
            new BooleanSetting("Не взрывать себя", true),
            new BooleanSetting("Коррекция движения", true)
    );

    private final SliderSetting delayAttack = new SliderSetting("Задержка", 5, 0, 10, 1);
    private Entity crystalEntity = null;
    private BlockPos obsidianPos = null;
    private int oldCurrentSlot = -1;
    public Vector2f rotationVector = new Vector2f(0, 0);
    TimerUtils attackTimerUtils = new TimerUtils();
    int bestSlot = -1;
    int oldSlot = -1;

    public boolean check() {
        return rotationVector != null && options.is("Коррекция движения").getValue() && crystalEntity != null && obsidianPos != null && isEnabled();
    }

    public AutoCrystal() {
        addSettings(options, delayAttack);
    }

    @Subscribe
    public void onMoveInput(EventInput e) {
        if (check()) {
            MoveUtils.fixMovement(e, rotationVector.x);
        }
    }

    @Subscribe
    public void onObsidianPlace(EventPlaceObsidian e) {
        BlockPos obsidianPos = e.getPos();

        boolean isOffHand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;

        int slotInInventory = InventoryUtils.getInstance().getSlotInInventoryOrHotbar(Items.END_CRYSTAL, false);
        int slotInHotBar = InventoryUtils.getInstance().getSlotInInventoryOrHotbar(Items.END_CRYSTAL, true);
        bestSlot = InventoryUtils.getInstance().findBestSlotInHotBar();
        boolean slotNotNull = mc.player.inventory.getStackInSlot(bestSlot).getItem() != Items.AIR;

        if (isOffHand) {
            if (obsidianPos != null) {
                setAndUseCrystal(bestSlot, obsidianPos);
                this.obsidianPos = obsidianPos;
            }
        }

        if (slotInHotBar == -1 && slotInInventory != -1 && bestSlot != -1) {
            InventoryUtils.moveItem(slotInInventory, bestSlot + 36, slotNotNull);
            if (slotNotNull && oldSlot == -1) {
                oldSlot = slotInInventory;
            }
            if (obsidianPos != null) {
                oldCurrentSlot = mc.player.inventory.currentItem;
                setAndUseCrystal(bestSlot, obsidianPos);
                mc.player.inventory.currentItem = oldCurrentSlot;
                this.obsidianPos = obsidianPos;
            }
            mc.playerController.windowClick(0, oldSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClickFixed(0, oldSlot, 0, ClickType.PICKUP, mc.player, 250);
        } else if (slotInHotBar != -1) {
            if (obsidianPos != null) {
                oldCurrentSlot = mc.player.inventory.currentItem;
                setAndUseCrystal(slotInHotBar, obsidianPos);
                mc.player.inventory.currentItem = oldCurrentSlot;
                this.obsidianPos = obsidianPos;
            }
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (obsidianPos != null)
            findEnderCrystals(obsidianPos).forEach(this::attackCrystal);

        if (crystalEntity != null)
            if (!crystalEntity.isAlive()) reset();
    }

    @Subscribe
    private void onMotion(EventMotion e) {
        if (isValid(crystalEntity)) {
            rotationVector = MathUtils.rotationToEntity(crystalEntity);
            e.setYaw(rotationVector.x);
            e.setPitch(rotationVector.y);
            mc.player.renderYawOffset = rotationVector.x;
            mc.player.rotationYawHead = rotationVector.x;
            mc.player.rotationPitchHead = rotationVector.y;

            if (options.is("Коррекция движения").getValue()) {
                mc.player.rotationYawOffset = rotationVector.x;
            }
        } else {
            if (options.is("Коррекция движения").getValue()) {
                mc.player.rotationYawOffset = Integer.MIN_VALUE;
            }
        }
    }

    @Override
    public void onDisable() {
        reset();
        super.onDisable();

        if (options.is("Коррекция движения").getValue()) {
            mc.player.rotationYawOffset = Integer.MIN_VALUE;
        }
    }

    private void attackCrystal(Entity entity) {
        if (isValid(entity) && mc.player.getCooledAttackStrength(1.0f) >= 1.0f && attackTimerUtils.hasTimeElapsed()) {
            long delay = delayAttack.getValue().longValue() * 100;
            attackTimerUtils.setLastMS(delay);
            mc.playerController.attackEntity(mc.player, entity);
            mc.player.swingArm(Hand.MAIN_HAND);
            crystalEntity = entity;
        }
        if (!entity.isAlive()) {
            reset();
        }
    }


    private void setAndUseCrystal(int slot, BlockPos pos) {
        boolean isOffHand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;

        Vector3d center = new Vector3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        if (!isOffHand)
            mc.player.inventory.currentItem = slot;


        Hand hand = isOffHand ? Hand.OFF_HAND : Hand.MAIN_HAND;

        if (mc.playerController.processRightClickBlock(
                mc.player, mc.world, hand,
                new BlockRayTraceResult(center, Direction.UP, pos, false)) == ActionResultType.SUCCESS) {
            mc.player.swingArm(Hand.MAIN_HAND);
        }
    }

    private boolean isValid(Entity base) {
        if (base == null) {
            return false;
        }
        if (obsidianPos == null) {
            return false;
        }
        if (options.is("Не взрывать себя").getValue() && mc.player.getPosY() > obsidianPos.getY()) {
            return false;
        }
        return isCorrectDistance();
    }

    private boolean isCorrectDistance() {
        if (obsidianPos == null) {
            return false;
        }
        return mc.player.getPositionVec().distanceTo(
                new Vector3d(obsidianPos.getX(),
                        obsidianPos.getY(),
                        obsidianPos.getZ())) <= mc.playerController.getBlockReachDistance();
    }

    public List<Entity> findEnderCrystals(BlockPos position) {
        return mc.world.getEntitiesWithinAABBExcludingEntity(null,
                        new AxisAlignedBB(
                                position.getX(),
                                position.getY(),
                                position.getZ(),
                                position.getX() + 1.0,
                                position.getY() + 2.0,
                                position.getZ() + 1.0))
                .stream()
                .filter(entity -> entity instanceof EnderCrystalEntity)
                .collect(Collectors.toList());
    }

    private void reset() {
        crystalEntity = null;
        obsidianPos = null;
        rotationVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
        oldCurrentSlot = -1;
        bestSlot = -1;
    }
}
