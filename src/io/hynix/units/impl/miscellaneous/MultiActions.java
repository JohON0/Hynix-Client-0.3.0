package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

@UnitRegister(name="MultiActions", category = Category.Miscellaneous, desc="Позволяет одновременно есть и копать", premium = true)
public class MultiActions extends Unit {

    EntityRayTraceResult entityRayTraceResult;
    BlockRayTraceResult blockRayTraceResult;

    RayTraceResult rayTraceResult = MultiActions.mc.objectMouseOver;

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (rayTraceResult instanceof BlockRayTraceResult && (blockRayTraceResult = (BlockRayTraceResult)rayTraceResult).getPos()
                != null && MultiActions.mc.gameSettings.keyBindAttack.isPressed() && !MultiActions.mc.world.getBlockState(blockRayTraceResult.getPos()).isAir()) {
            MultiActions.mc.playerController.clickBlock(blockRayTraceResult.getPos(), blockRayTraceResult.getFace());
            MultiActions.mc.player.swingArm(Hand.MAIN_HAND);
        }

        if ((rayTraceResult = MultiActions.mc.objectMouseOver) instanceof EntityRayTraceResult && (entityRayTraceResult = (EntityRayTraceResult)rayTraceResult).getEntity() != null
                && MultiActions.mc.gameSettings.keyBindAttack.isPressed() && MultiActions.mc.player.getCooledAttackStrength(0.5f) > 0.9f) {
            MultiActions.mc.playerController.attackEntity(MultiActions.mc.player, entityRayTraceResult.getEntity());
            MultiActions.mc.player.swingArm(Hand.MAIN_HAND);
        }
    }
}
