package io.hynix.units.impl.display;

import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.SliderSetting;

@UnitRegister(name = "AspectRatio", category = Category.Display, desc = "Меняет формат разрешения")

public class AspectRatio extends Unit {
    public static SliderSetting value = new SliderSetting("Значение",1.78f, 0.1f, 5f,0.01f);

    public AspectRatio(){
        addSettings(value);
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
