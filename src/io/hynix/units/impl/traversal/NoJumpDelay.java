package io.hynix.units.impl.traversal;

import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;

@UnitRegister(name = "NoJumpDelay", category = Category.Traversal, desc = "Убирает задержку при прыжке")
public class NoJumpDelay extends Unit {

    private void onUpdate(EventUpdate e) {
        mc.player.jumpTicks = 0;
    }
}
