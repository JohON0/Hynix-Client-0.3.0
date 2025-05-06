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
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;


@UnitRegister(name = "Strafe", category = Category.Traversal, desc = "Стрейфы в майнкрафте чтоооооо??!?!?!")
public class Strafe extends Unit {
    private final BooleanSetting damageBoost = new BooleanSetting("Буст с дамагом", false);

    private final SliderSetting boostSpeed = new SliderSetting("Значение буста", 0.7f, 0.1F, 5.0f, 0.1F);
    private final DamagePlayerUtils damageUtil = new DamagePlayerUtils();
    private final StrafeMovement strafeMovement = new StrafeMovement();

    private final TargetStrafe targetStrafe;
    private final AttackAura killAura;

    public Strafe(TargetStrafe targetStrafe, AttackAura killAura) {
        this.targetStrafe = targetStrafe;
        this.killAura = killAura;
        addSettings(damageBoost, boostSpeed);
    }

    @Subscribe
    private void onAction(ActionEvent e) {
        handleEventAction(e);
    }

    @Subscribe
    private void onMoving(EventMoving e) {
        handleEventMove(e);
    }

    @Subscribe
    private void onPostMove(EventPostMove e) {
        handleEventPostMove(e);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        handleEventPacket(e);
    }

    @Subscribe
    private void onDamage(EventDamageReceive e) {
        handleDamageEvent(e);
    }

    private void handleDamageEvent(EventDamageReceive damage) {
        if (damageBoost.getValue()) {
            damageUtil.processDamage(damage);
        }
    }

    private void handleEventAction(ActionEvent action) {
        if (strafes()) {
            handleStrafesEventAction(action);
        }
        if (strafeMovement.isNeedSwap()) {
            handleNeedSwapEventAction(action);
        }
    }

    private void handleEventMove(EventMoving eventMove) {
        if (strafes()) {
            handleStrafesEventMove(eventMove);
        } else {
            strafeMovement.setOldSpeed(0);
        }
    }

    private void handleEventPostMove(EventPostMove eventPostMove) {
        strafeMovement.postMove(eventPostMove.getHorizontalMove());
    }

    private void handleEventPacket(EventPacket packet) {

        if (packet.getType() == EventPacket.Type.RECEIVE) {
            if (damageBoost.getValue()) {
                damageUtil.onPacketEvent(packet);
            }
            handleReceivePacketEventPacket(packet);
        }
    }

    private void handleStrafesEventAction(ActionEvent action) {
        if (CEntityActionPacket.lastUpdatedSprint != strafeMovement.isNeedSprintState()) {
            action.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
        }
    }

    private void handleStrafesEventMove(EventMoving eventMove) {
        if (targetStrafe.isEnabled() && (killAura.isEnabled() && killAura.target != null)) {
            return;
        }


        if (damageBoost.getValue())
            this.damageUtil.time(700L);

        final float damageSpeed = boostSpeed.getValue().floatValue() / 10.0F;
        final double speed = strafeMovement.calculateSpeed(eventMove, damageBoost.getValue(), damageUtil.isNormalDamage(), false, damageSpeed);

        MoveUtils.MoveEvent.setMoveMotion(eventMove, speed);
    }

    private void handleNeedSwapEventAction(ActionEvent action) {
        action.setSprintState(!mc.player.serverSprintState);
        strafeMovement.setNeedSwap(false);
    }

    private void handleReceivePacketEventPacket(EventPacket packet) {
        if (packet.getPacket() instanceof SPlayerPositionLookPacket) {
            strafeMovement.setOldSpeed(0);
        }

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

    @Override
    public void onEnable() {
        strafeMovement.setOldSpeed(0);
        super.onEnable();
    }
}
