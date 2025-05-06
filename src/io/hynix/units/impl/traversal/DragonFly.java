package io.hynix.units.impl.traversal;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventMotion;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.utils.player.MoveUtils;

@UnitRegister(name = "DragonFly", category = Category.Traversal, desc = "Позволяет летать быстрее")
public class DragonFly extends Unit {

    @Subscribe
    public void onMotion(EventMotion e) {
        if (mc.player.abilities.isFlying) {
            MoveUtils.setMotion(1.05);
            mc.player.motion.y = 0.0;
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.motion.y = 0.35;
                if (mc.player.moveForward == 0.0f && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown()) {
                    mc.player.motion.y = 0.8;
                }
            }

            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.motion.y = -0.35;
                if (mc.player.moveForward == 0.0f && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown()) {
                    mc.player.motion.y = -0.8;
                }
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
