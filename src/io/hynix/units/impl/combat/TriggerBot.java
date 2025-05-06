package io.hynix.units.impl.combat;

import com.google.common.eventbus.Subscribe;

import io.hynix.managers.friend.FriendManager;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.AttackUtils;
import io.hynix.utils.player.InventoryUtils;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
/**
 * @author JohON0
 */
@UnitRegister(name = "TriggerBot", category = Category.Combat, desc = "Не дает отталкиваться при ударе либо получении урона")
public class TriggerBot extends Unit {

    private final BooleanSetting onlyCrit = new BooleanSetting("Только криты", true);
    private final BooleanSetting smartCrit = new BooleanSetting("Умные криты", true).setVisible(() -> onlyCrit.getValue());
    private final BooleanSetting players = new BooleanSetting("Игроки", true);
    private final BooleanSetting mobs = new BooleanSetting("Мобы", false);
    private final BooleanSetting animals = new BooleanSetting("Животные", false);
    private final BooleanSetting shieldBreak = new BooleanSetting("Ломать щит", true);
    private final BooleanSetting tpsSync = new BooleanSetting("TPSSync", false);

    public TriggerBot() {
        addSettings(onlyCrit, smartCrit, players, mobs, animals, shieldBreak, tpsSync);
    }

    private final TimerUtils timerUtils = new TimerUtils();
    private final TimerUtils timerForTarget = new TimerUtils();
    @Getter
    LivingEntity target = null;


    @Subscribe
    public void onUpdate(EventUpdate e) {
        Entity entity = getValidEntity();

        target = (LivingEntity) entity;

        if (entity == null || mc.player == null) {
            return;
        }

        if (shouldAttack()) {
            timerUtils.setLastMS(500);
            attackEntity(entity);
        }
    }

    private boolean shouldAttack() {
        return AttackUtils.isPlayerFalling(onlyCrit.getValue(), smartCrit.getValue(), tpsSync.getValue(), true) && (timerUtils.hasTimeElapsed());
    }

    private void attackEntity(Entity entity) {
        boolean shouldStopSprinting = false;
        if (onlyCrit.getValue() && CEntityActionPacket.lastUpdatedSprint) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
            shouldStopSprinting = true;
        }

        mc.playerController.attackEntity(mc.player, entity);
        mc.player.swingArm(Hand.MAIN_HAND);
        if (shieldBreak.getValue() && entity instanceof PlayerEntity)
            breakShieldPlayer(entity);

        if (shouldStopSprinting) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
        }
    }

    private Entity getValidEntity() {
        if (mc.objectMouseOver.getType() == RayTraceResult.Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) mc.objectMouseOver).getEntity();

            if (checkEntity((LivingEntity) entity != null ? (LivingEntity) entity : null)) {
                return entity;
            }
        }
        return null;
    }

    public static void breakShieldPlayer(Entity entity) {
        if (((LivingEntity) entity).isBlocking()) {
            int invSlot = InventoryUtils.getInstance().getAxeInInventory(false);
            int hotBarSlot = InventoryUtils.getInstance().getAxeInInventory(true);

            if (hotBarSlot == -1 && invSlot != -1) {
                int bestSlot = InventoryUtils.getInstance().findBestSlotInHotBar();
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);

                mc.player.connection.sendPacket(new CHeldItemChangePacket(bestSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));

                mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, mc.player);
            }

            if (hotBarSlot != -1) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hotBarSlot));
                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(Hand.MAIN_HAND);
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
        }
    }

    private boolean checkEntity(LivingEntity entity) {
        if (FriendManager.isFriend(entity.getName().getString())) {
            return false;
        }

        AttackUtils entitySelector = new AttackUtils();

        if (players.getValue()) {
            entitySelector.apply(AttackUtils.EntityType.PLAYERS);
        }
        if (mobs.getValue()) {
            entitySelector.apply(AttackUtils.EntityType.MOBS);
        }
        if (animals.getValue()) {
            entitySelector.apply(AttackUtils.EntityType.ANIMALS);
        }
        return entitySelector.ofType(entity, entitySelector.build()) != null && entity.isAlive();
    }

    @Override
    public void onDisable() {
        timerUtils.reset();
        timerForTarget.reset();
        target = null;
        super.onDisable();
    }
}
