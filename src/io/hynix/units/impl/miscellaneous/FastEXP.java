package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

@UnitRegister(name = "FastEXP", category = Category.Miscellaneous, desc = "Ускоряет получение опыта")
public class FastEXP extends Unit {
    @Subscribe
    private void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.getHeldItem(Hand.MAIN_HAND).getItem() == Items.EXPERIENCE_BOTTLE) {
            mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
        }
    }
}
