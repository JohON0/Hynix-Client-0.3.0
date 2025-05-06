package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventPacket;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeListSetting;
import lombok.Getter;
import net.minecraft.network.play.server.SExplosionPacket;
@Getter
@UnitRegister(name = "AntiPush", category = Category.Miscellaneous, desc = "Убирает отталкивание")
public class AntiPush extends Unit {

    public static ModeListSetting modes = new ModeListSetting("Тип",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Вода", false),
            new BooleanSetting("Взрывы", false),
            new BooleanSetting("Блоки", true));

    public AntiPush() {
        addSettings(modes);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.isReceive()) {
            if (modes.is("Взрывы").getValue()) {
                if (e.getPacket() instanceof SExplosionPacket) {
                    e.cancel();
                }
            }
        }
    }
}
