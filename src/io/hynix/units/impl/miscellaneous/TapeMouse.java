package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.johon0.math.TimerUtils;

@UnitRegister(name = "TapeMouse", category = Category.Miscellaneous, desc = "Сам кликает мышкой")
public class TapeMouse extends Unit {
    final SliderSetting delay = new SliderSetting("Задержка", 5f, 0f, 10f, 1f);
    TimerUtils timerUtils = new TimerUtils();
    public TapeMouse() {
        addSettings(delay);
    }
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (timerUtils.isReached(1000L * delay.getValue().longValue())) {
        if (mc.player.getCooledAttackStrength(1f) >= 1) {
            mc.clickMouse();
            timerUtils.reset();
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
