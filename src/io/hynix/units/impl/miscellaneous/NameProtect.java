package io.hynix.units.impl.miscellaneous;

import io.hynix.HynixMain;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

@UnitRegister(name = "NameProtect", category = Category.Miscellaneous, desc = "Скрывает ваш никнейм")
public class NameProtect extends Unit {
    public static String getReplaced(String input) {
        if (HynixMain.getInstance() != null && HynixMain.getInstance().getModuleManager().getNameProtect().isEnabled()) {
            input = input.replace(Minecraft.getInstance().session.getUsername(), TextFormatting.RED + "LitvinAntiLeak" + TextFormatting.RESET);
        }
        return input;
    }
}
