package io.hynix.units.impl.miscellaneous;

import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.units.settings.impl.SliderSetting;

import static java.lang.Math.signum;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@UnitRegister(name = "Sounds", category = Category.Miscellaneous, desc = "Звуки включения/выключения модуля")
public class Sounds extends Unit {

    public ModeSetting mode = new ModeSetting("Тип", "Default", "Default", "Windows", "Droplet");
    public SliderSetting volume = new SliderSetting("Громкость", 60.0f, 0.0f, 100.0f, 1.0f);

    public Sounds() {
        addSettings(mode, volume);
        toggle();
    }

    public String getFileName(boolean state) {
        switch (mode.getValue()) {
            case "Default" -> {
                return state ? "enabled" : "disabled";
            }
            case "Windows" -> {
                return state ? "winenable" : "windisable";
            }
            case "Droplet" -> {
                return state ? "dropletenable" : "dropletdisable";
            }
        }
        return "";
    }
}
