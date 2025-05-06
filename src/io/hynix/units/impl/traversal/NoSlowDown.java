package io.hynix.units.impl.traversal;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventNoSlow;
import io.hynix.events.impl.EventPacket;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.MoveUtils;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.util.Hand;

@UnitRegister(name = "NoSlowDown",category = Category.Traversal, desc = "Убирает замедление при использовании предмета")
public class NoSlowDown extends Unit {
    public TimerUtils timerUtil = new TimerUtils();
    boolean closeMe = false;
    boolean eActiveNoSlow = false;

    public final ModeSetting noSlowMode = new ModeSetting("Mode", "Matrix", "Matrix","HolyWorld");
    private final SliderSetting speedReduction = new SliderSetting("Сила", 1.0F, 0.1F, 1.0F, 0.1F).setVisible(() -> noSlowMode.is("Matrix"));

    public NoSlowDown() {
        addSettings(noSlowMode,speedReduction);
    }
    @Subscribe
    public void onPacket(EventPacket e) {
        if (noSlowMode.is("Damage")) {
            if (e.getPacket() instanceof SEntityVelocityPacket && ((SEntityVelocityPacket)e.getPacket()).getEntityID() == mc.player.getEntityId()) {
                closeMe = true;
                timerUtil.reset();
            }

            if (closeMe && timerUtil.hasTimeElapsed(1600, false)) {
                closeMe = false;
                timerUtil.reset();
            }
        }
    }

    @Subscribe
    public void onEating(EventNoSlow e) {
        handleEventUpdate(e);
    }

    private void handleEventUpdate(EventNoSlow eventNoSlow) {
        if (mc.player.isHandActive()) {
            switch (noSlowMode.getValue()) {
//                case "Grim" -> handleGrimMode(eventNoSlow);
                case "Matrix" -> handleMatrixMode(eventNoSlow);
                case "HolyWorld" -> handleHolyWorldMode(eventNoSlow);
//                case "GrimLast" -> handleHWMode(eventNoSlow);
//                case "Dagame" -> handleDamageMode(eventNoSlow);
            }
        } else {
            eActiveNoSlow = false;
            mc.timer.timerSpeed = 1f;
        }
    }
    private void handleDamageMode(EventNoSlow e) {
        if ((mc.player.isInWater() || closeMe) && mc.player.getActiveHand() == Hand.MAIN_HAND && mc.player.getHeldItemOffhand().getUseAction() == UseAction.NONE) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            e.cancel();
        }

        if (mc.player.getActiveHand() == Hand.OFF_HAND && MoveUtils.isMoving()) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            e.cancel();
        }
    }

    private void handleHWMode(EventNoSlow event) {
        boolean mainHandActive;
        boolean offHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.OFF_HAND;
        boolean bl = mainHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.MAIN_HAND;
        if ((mc.player.getItemInUseCount() < 28 && mc.player.getItemInUseCount() > 4 || mc.player.getHeldItemOffhand().getItem() == Items.SHIELD) && mc.player.isHandActive() && !mc.player.isPassenger()) {
            mc.playerController.syncCurrentPlayItem();
            if (offHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemOffhand().getItem())) {
                int old = mc.player.inventory.currentItem;
                mc.player.connection.sendPacket(new CHeldItemChangePacket(old + 1 > 8 ? old - 1 : old + 1));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                mc.player.setSprinting(false);
                event.cancel();
            }
            if (mainHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemMainhand().getItem())) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                if (mc.player.getHeldItemOffhand().getUseAction().equals((Object)UseAction.NONE)) {
                    event.cancel();
                }
            }
            mc.playerController.syncCurrentPlayItem();
        }
    }

    private void handleMatrixMode(EventNoSlow eventNoSlow) {
        boolean isFalling = (double) mc.player.fallDistance > 0.725;
        float speedMultiplier;
        eventNoSlow.cancel();

        if (mc.player.isOnGround() && !mc.player.movementInput.jump) {
            if (mc.player.ticksExisted % 2 == 0) {
                boolean isNotStrafing = mc.player.moveStrafing == 0.0F;
                speedMultiplier = isNotStrafing ? speedReduction.getValue() : speedReduction.getValue() * 0.8F; // Используйте значение слайдера
                mc.player.motion.x *= speedMultiplier;
                mc.player.motion.z *= speedMultiplier;
            }
        } else if (isFalling) {
            boolean isVeryFastFalling = (double) mc.player.fallDistance > 1.4;
            speedMultiplier = isVeryFastFalling ? speedReduction.getValue() * 0.95F : speedReduction.getValue() * 0.97F; // Используйте значение слайдера
            mc.player.motion.x *= speedMultiplier;
            mc.player.motion.z *= speedMultiplier;
        }
    }

    private void handleHolyWorldMode(EventNoSlow e) {
        if ((mc.player.getHeldItemOffhand().getUseAction() != UseAction.BLOCK || mc.player.getActiveHand() != Hand.MAIN_HAND) && (mc.player.getHeldItemOffhand().getUseAction() != UseAction.EAT || mc.player.getActiveHand() != Hand.MAIN_HAND)) {
            if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
                e.cancel();
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            } else {
                e.cancel();
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            }
        }
    }

    private void handleGrimMode(EventNoSlow noSlow) {
        boolean offHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.OFF_HAND;
        boolean mainHandActive = mc.player.isHandActive() && mc.player.getActiveHand() == Hand.MAIN_HAND;
        if (!(mc.player.getItemInUseCount() < 25 && mc.player.getItemInUseCount() > 4) && mc.player.getHeldItemOffhand().getItem() != Items.SHIELD) {
            return;
        }
        if (mc.player.isHandActive() && !mc.player.isPassenger()) {
            mc.playerController.syncCurrentPlayItem();
            if (offHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemOffhand().getItem())) {
                int old = mc.player.inventory.currentItem;
                mc.player.connection.sendPacket(new CHeldItemChangePacket(old + 1 > 8 ? old - 1 : old + 1));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                mc.player.setSprinting(false);
                noSlow.cancel();
            }

            if (mainHandActive && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemMainhand().getItem())) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));

                if (mc.player.getHeldItemOffhand().getUseAction().equals(UseAction.NONE)) {
                    noSlow.cancel();
                }
            }
            mc.playerController.syncCurrentPlayItem();
        }
    }
}
