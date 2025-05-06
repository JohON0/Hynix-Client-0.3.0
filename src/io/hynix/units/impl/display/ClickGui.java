package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.SliderSetting;
import org.lwjgl.glfw.GLFW;

@UnitRegister(name = "ClickGui", category = Category.Display, desc = "Настройка Меню чита")
public class ClickGui extends Unit {

    public static BooleanSetting gradient = new BooleanSetting("Градиент", true);
    public static BooleanSetting background = new BooleanSetting("Фон", true);
    public static BooleanSetting blur = new BooleanSetting("Размыть", false);
    public static BooleanSetting snow = new BooleanSetting("Снег", true);
    public static SliderSetting blurPower = new SliderSetting("Сила размытия", 2, 1, 4, 1).setVisible(() -> blur.getValue());

    public ClickGui() {
        addSettings(background, gradient, snow, blur, blurPower);
        setBind(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        toggle();
    }
}
