package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventPacket;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;

@UnitRegister(name = "RPSpoofer", category = Category.Miscellaneous, desc = "Пропускает скачивания ресурспака на серверах")
public class RPSpoofer extends Unit {

    @Subscribe
    public void onPacket(EventPacket e) {

        if (e.getPacket() instanceof SSendResourcePackPacket) {
            mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.ACCEPTED));
            mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED));
            if (mc.currentScreen != null) {
                mc.player.closeScreen();
            }
            e.cancel();
        }
    }
}
