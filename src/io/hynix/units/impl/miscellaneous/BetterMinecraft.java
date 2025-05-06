package io.hynix.units.impl.miscellaneous;

import io.hynix.HynixMain;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;

@UnitRegister(name = "BetterMinecraft", category = Category.Miscellaneous, desc = "Улучшение игры")
public class BetterMinecraft extends Unit {

    public final BooleanSetting smoothCamera = new BooleanSetting("Плавная камера", true);
    public final BooleanSetting smoothTab = new BooleanSetting("Плавный таб", true);
    public final BooleanSetting smoothChat = new BooleanSetting("Плавный чат", true);
    public final BooleanSetting betterTab = new BooleanSetting("Улучшенный таб", true);
    public final BooleanSetting betterChat = new BooleanSetting("Улучшенный чат", true);

    public static BooleanSetting fpsBoot = new BooleanSetting("Оптимизировать", false);

    public BetterMinecraft() {
        addSettings(betterTab, betterChat, smoothCamera, smoothTab, smoothChat,fpsBoot);
    }

    public static boolean isFpsMode() {
        return HynixMain.getInstance().getModuleManager().getBetterMinecraft().isEnabled() && fpsBoot.getValue();
    }
}
