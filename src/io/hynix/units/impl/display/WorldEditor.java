package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;
import io.hynix.HynixMain;
import io.hynix.events.impl.EventPacket;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.*;
import io.hynix.ui.notifications.impl.WarningNotify;
import io.hynix.utils.johon0.render.color.ColorUtils;
import net.minecraft.network.play.server.SUpdateTimePacket;
/**
 * @author JohON0
 */
@UnitRegister(name = "WorldEditor", category = Category.Display, desc = "Редакция мира в игре")
public class WorldEditor extends Unit {

    public static ModeListSetting options = new ModeListSetting("Опции",
            new BooleanSetting("Кастомный туман", true),
            new BooleanSetting("Своя дистанция тумана", true),
            new BooleanSetting("Физика предметов", true),
            new BooleanSetting("Время", true)
    );

    public static SliderSetting fogDistance = new SliderSetting("Дистация тумана", 0.4f, 0.1f, 1, 0.1f).setVisible(() -> options.is("Своя дистанция тумана").getValue());

    public static ModeSetting mode = new ModeSetting("Вид", "Клиент", "Клиент", "Свой").setVisible(() -> options.is("Кастомный туман").getValue());

    public static ColorSetting colorFog = new ColorSetting("Цвет тумана", ColorUtils.rgb(255, 255, 255)).setVisible(() -> mode.is("Свой"));

    public static ModeSetting time = new ModeSetting("Время", "День", "День", "Ночь").setVisible(() -> options.is("Время").getValue());



    public WorldEditor() {
        addSettings(options, fogDistance, mode, time, colorFog);
    }


    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SUpdateTimePacket p) {
            if (isEnabled("Время")) {
                if (time.is("Ночь"))
                    p.worldTime = 18000L;
                else
                    p.worldTime = 1000L;
            }
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if ((!isEnabled("Кастомный туман") &&
                !isEnabled("Время") &&
                !isEnabled("Физика предметов") &&
                !isEnabled("Своя дистанция тумана")
        )) {
            toggle();
            HynixMain.getInstance().getNotifyManager().add(0, new WarningNotify("Включите что-нибудь!", 3000));
        }
    }

    public boolean isEnabled(String name) {
        return options.is(name).getValue();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
