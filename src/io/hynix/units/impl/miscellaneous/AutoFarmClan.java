package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventMotion;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import net.minecraft.block.AirBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

@UnitRegister(name = "AutoFarmClan", category = Category.Miscellaneous, desc = "Фармит уровень клана за вас")
public class AutoFarmClan extends Unit {
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.world.getBlockState(mc.player.getPosition()).getBlock() instanceof AirBlock) {
            mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockRayTraceResult(mc.player.getPositionVec().subtract(0, 1, 0), Direction.UP, mc.player.getPosition().down(), false));
            mc.player.swingArm(Hand.MAIN_HAND);
        } else {
            mc.playerController.onPlayerDamageBlock(mc.player.getPosition(), Direction.UP);
            mc.player.swingArm(Hand.MAIN_HAND);
        }
    }
    @Subscribe
    public void onMotion(EventMotion e) {
        e.setPitch(89);
        mc.player.rotationPitchHead = e.getPitch();
    }
}
