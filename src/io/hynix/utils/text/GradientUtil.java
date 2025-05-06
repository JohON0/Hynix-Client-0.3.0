package io.hynix.utils.text;

import io.hynix.managers.theme.Theme;
import io.hynix.utils.johon0.render.color.ColorUtils;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

public class GradientUtil {

    public static StringTextComponent gradient(String message) {
        StringTextComponent text = new StringTextComponent("");
        for (int i = 0; i < message.length(); i++) {
            text.append(new StringTextComponent(String.valueOf(message.charAt(i))).setStyle(Style.EMPTY.setColor(new Color(Theme.rectColor))));
        }
        return text;
    }
    public static StringTextComponent goldText(String message) {
        StringTextComponent text = new StringTextComponent("");
        for (int i = 0; i < message.length(); i++) {
            text.append(new StringTextComponent(String.valueOf(message.charAt(i))).setStyle(Style.EMPTY.setColor(new Color(ColorUtils.rgba(255,215,0,255)))));
        }
        return text;
    }

}
