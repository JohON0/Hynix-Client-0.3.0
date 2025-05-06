package io.hynix.units.impl.traversal;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.*;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.impl.combat.AttackAura;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.player.DamagePlayerUtils;
import io.hynix.utils.player.MoveUtils;
import io.hynix.utils.player.StrafeMovement;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;


@UnitRegister(name = "TargetStrafe", category = Category.Traversal, desc = "Позволяет стрейфиться вокруг Entity (использовать с AttackAura)")
public class TargetStrafe extends Unit {

    private final SliderSetting distanceSetting = new SliderSetting("Дистанция", 1f, 0.1f, 6f, 0.05f);
    private final SliderSetting speedSetting = new SliderSetting("Скорость", 0f, -0.1f, 0.5f, 0.01f);
    private final BooleanSetting autoJump = new BooleanSetting("Авто прыжок", true);
    private final BooleanSetting damageBoostSetting = new BooleanSetting("Буст с дамагом", true);
    private final SliderSetting boostValueSetting = new SliderSetting("Значение буста", 1.5F, 0.1f, 5.0f, 0.05f).setVisible(() -> damageBoostSetting.getValue());
    private final SliderSetting timeSetting = new SliderSetting("Время буста", 10.0f, 1.0f, 20.0f, 1.0f).setVisible(() -> damageBoostSetting.getValue());
    private final BooleanSetting saveTarget = new BooleanSetting("Сохранять цель", true);


    private float side = 1;
    private LivingEntity target = null;
    private final DamagePlayerUtils damageUtil = new DamagePlayerUtils();
    private String targetName = "";
    public StrafeMovement strafeMovement = new StrafeMovement();
    private final AttackAura hitAura;

    public TargetStrafe(AttackAura hitAura) {
        this.hitAura = hitAura;
        addSettings(distanceSetting, speedSetting, autoJump, saveTarget, damageBoostSetting, timeSetting);
    }

    @Subscribe
    private void onAction(ActionEvent e) {
        if (mc.player == null || mc.world == null) return;
        handleEventAction(e);
    }

    @Subscribe
    public void onMotion(final EventMoving event) {

        if (mc.player == null || mc.world == null || mc.player.ticksExisted < 10) return;
        boolean isLeftKeyPressed = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_A);
        boolean isRightKeyPressed = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_D);

        LivingEntity auraTarget = getTarget();

        if (auraTarget != null) {
            targetName = auraTarget.getName().getString();
        }

        if (shouldSaveTarget(auraTarget)) {
            target = updateTarget(target);
        } else target = auraTarget;

        if (target != null && target.isAlive() && target.getHealth() > 0.0f) {
            if (mc.player.collidedHorizontally) side *= -1;
            if (isLeftKeyPressed) side = 1;
            if (isRightKeyPressed) side = -1;

            double angle = Math.atan2(mc.player.getPosZ() - target.getPosZ(), mc.player.getPosX() - target.getPosX());
            angle += MoveUtils.getMotion() / Math.max(mc.player.getDistance(target), distanceSetting.min) * side;

            double x = target.getPosX() + distanceSetting.getValue() * Math.cos(angle);
            double z = target.getPosZ() + distanceSetting.getValue() * Math.sin(angle);

            double yaw = getYaw(mc.player, x, z);

            this.damageUtil.time(timeSetting.getValue().longValue() * 100);
            final float damageSpeed = boostValueSetting.getValue() / 10.0F;
            double currentDistance = mc.player.getDistance(target);
            double speedAdjustment = (currentDistance <= distanceSetting.getValue() - 0.1) ? speedSetting.getValue() : 0;

            final double speed = strafeMovement.calculateSpeed(event, damageBoostSetting.getValue(), damageUtil.isNormalDamage(), true, damageSpeed) + speedAdjustment;
            event.getMotion().x = (speed * -Math.sin(Math.toRadians(yaw)));
            event.getMotion().z = (speed * Math.cos(Math.toRadians(yaw)));
        }
    }

    @Subscribe
    private void onPostMove(EventPostMove e) {
        if (mc.player == null || mc.world == null) return;
        strafeMovement.postMove(e.getHorizontalMove());
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (mc.player == null || mc.world == null) return;
        if (e.getType() == EventPacket.Type.RECEIVE) {
            damageUtil.onPacketEvent(e);
            if (e.getPacket() instanceof SPlayerPositionLookPacket) {
                strafeMovement.setOldSpeed(0);
            }
        }
    }

    @Subscribe
    private void onDamage(EventDamageReceive e) {
        if (mc.player == null || mc.world == null) return;
        damageUtil.processDamage(e);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player.isOnGround() && autoJump.getValue() && !mc.gameSettings.keyBindJump.pressed && target != null && target.isAlive()) {
            mc.player.jump();
        }
    }

    @Override
    public void onEnable() {
        strafeMovement.setOldSpeed(0);
        target = null;
        super.onEnable();
    }

    private void handleEventAction(ActionEvent action) {
        if (strafes()) {
            if (CEntityActionPacket.lastUpdatedSprint != strafeMovement.isNeedSprintState()) {
                action.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
            }
        }
        if (strafeMovement.isNeedSwap()) {
            action.setSprintState(!mc.player.serverSprintState);
            strafeMovement.setNeedSprintState(false);
        }
    }

    private LivingEntity getTarget() {
        return hitAura.isEnabled() ? hitAura.target : null;
    }

    private LivingEntity updateTarget(LivingEntity currentTarget) {
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof PlayerEntity && entity.getName().getString().equalsIgnoreCase(targetName)) {
                return (LivingEntity) entity;
            }
        }
        return currentTarget;
    }

    private boolean shouldSaveTarget(LivingEntity target) {
        boolean settingIsEnabled = saveTarget.getValue();
        boolean targetAndTargetNameExist = target != null && targetName != null;

        return settingIsEnabled && targetAndTargetNameExist && hitAura.isEnabled();
    }

    private double getYaw(LivingEntity entity, double x, double z) {
        return Math.toDegrees(Math.atan2(z - entity.getPosZ(), x - entity.getPosX())) - 90F;
    }

    public boolean strafes() {
        if (isInvalidPlayerState()) {
            return false;
        }

        BlockPos playerPosition = new BlockPos(mc.player.getPositionVec());
        BlockPos abovePosition = playerPosition.up();
        BlockPos belowPosition = playerPosition.down();

        if (isSurfaceLiquid(abovePosition, belowPosition)) {
            return false;
        }

        if (isPlayerInWebOrSoulSand(playerPosition)) {
            return false;
        }

        return isPlayerAbleToStrafe();
    }

    private boolean isInvalidPlayerState() {
        return mc.player == null || mc.world == null
                || mc.player.isSneaking()
                || mc.player.isElytraFlying()
                || mc.player.isInWater()
                || mc.player.isInLava();
    }

    private boolean isSurfaceLiquid(BlockPos abovePosition, BlockPos belowPosition) {
        Block aboveBlock = mc.world.getBlockState(abovePosition).getBlock();
        Block belowBlock = mc.world.getBlockState(belowPosition).getBlock();

        return aboveBlock instanceof AirBlock && belowBlock == Blocks.WATER;
    }

    private boolean isPlayerInWebOrSoulSand(BlockPos playerPosition) {
        Material playerMaterial = mc.world.getBlockState(playerPosition).getMaterial();
        Block oneBelowBlock = mc.world.getBlockState(playerPosition.down()).getBlock();

        return playerMaterial == Material.WEB || oneBelowBlock instanceof SoulSandBlock;
    }

    private boolean isPlayerAbleToStrafe() {
        return !mc.player.abilities.isFlying && !mc.player.isPotionActive(Effects.LEVITATION);
    }

}
