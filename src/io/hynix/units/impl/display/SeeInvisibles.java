package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import net.minecraft.entity.player.PlayerEntity;

@UnitRegister(name = "SeeInvisibles", category = Category.Display,desc = "Показывает невидимок")
public class SeeInvisibles extends Unit {


    @Subscribe
    private void onUpdate(EventUpdate e) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && player.isInvisible()) {
                player.setInvisible(false);
            }
        }
    }
}
