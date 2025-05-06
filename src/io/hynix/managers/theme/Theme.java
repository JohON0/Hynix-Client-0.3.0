package io.hynix.managers.theme;

import com.google.common.eventbus.Subscribe;
import io.hynix.HynixMain;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.impl.display.Hud;
import io.hynix.utils.johon0.render.color.ColorUtils;

import java.util.HashMap;
import java.util.Map;

public class Theme {

    public static int textColor, darkTextColor, mainRectColor, darkMainRectColor, rectColor;

    private static final Map<String, int[]> THEME_COLORS = new HashMap<>();

    static {
        // Зеленая
        THEME_COLORS.put("Зеленая", new int[]{
                ColorUtils.rgb(0, 128, 0),       // Темно-зеленый
                ColorUtils.rgb(34, 139, 34),     // Лесной зеленый
                ColorUtils.rgb(0, 255, 0),       // Ярко-зеленый
                ColorUtils.rgb(144, 238, 144),   // Светло-зеленый
                ColorUtils.rgb(50, 205, 50)      // Лайм
        });

        // Красная
        THEME_COLORS.put("Красная", new int[]{
                ColorUtils.rgb(255, 0, 0),       // Ярко-красный
                ColorUtils.rgb(220, 20, 60),     // Красный малина
                ColorUtils.rgb(255, 99, 71),     // Помидор
                ColorUtils.rgb(255, 182, 193),    // Розовый
                ColorUtils.rgb(178, 34, 34)      // Огненно-красный
        });

        // Темнокрасная
        THEME_COLORS.put("Темно-красная", new int[]{
                ColorUtils.rgb(139, 0, 0),       // Темно-красный
                ColorUtils.rgb(205, 0, 0),       // Красный
                ColorUtils.rgb(220, 20, 60),     // Красный малина
                ColorUtils.rgb(255, 36, 0),      // Ярко-красный
                ColorUtils.rgb(255, 99, 71)      // Помидор
        });

        // Синяя
        THEME_COLORS.put("Синяя", new int[]{
                ColorUtils.rgb(0, 0, 255),       // Ярко-синий
                ColorUtils.rgb(30, 144, 255),    // Светло-синий
                ColorUtils.rgb(70, 130, 180),    // Стальной синий
                ColorUtils.rgb(100, 149, 237),   // Кобальтовый
                ColorUtils.rgb(65, 105, 225)     // Королевский синий
        });

        // Темно-синяя
        THEME_COLORS.put("Темно-синяя", new int[]{
                ColorUtils.rgb(0, 0, 139),       // Темно-синий
                ColorUtils.rgb(0, 0, 205),       // Ярко-темно-синий
                ColorUtils.rgb(25, 25, 112),     // Темно-синяя
                ColorUtils.rgb(70, 130, 180),    // Стальной синий
                ColorUtils.rgb(65, 105, 225)     // Королевский синий
        });

        // Желтая
        THEME_COLORS.put("Желтая", new int[]{
                ColorUtils.rgb(236, 240, 0),     // Ярко-желтый
                ColorUtils.rgb(218, 222, 4),      // Золотой
                ColorUtils.rgb(181, 184, 29),    // Светло-желтый
                ColorUtils.rgb(247, 250, 87),     // Пастельный желтый
                ColorUtils.rgb(250, 252, 116)     // Очень светло-желтый
        });

        // Оранжевая
        THEME_COLORS.put("Оранжевая", new int[]{
                ColorUtils.rgb(252, 93, 0),      // Оранжевый
                ColorUtils.rgb(199, 86, 20),       // Темный оранжевый
                ColorUtils.rgb(232, 87, 2),       // Помидор
                ColorUtils.rgb(252, 129, 58),        // Ярко-оранжевый
                ColorUtils.rgb(255, 145, 82)      // Светло-оранжевый
        });

        // Фиолетовая
        THEME_COLORS.put("Фиолетовая", new int[]{
                ColorUtils.rgb(128, 0, 128),     // Фиолетовый
                ColorUtils.rgb(147, 112, 219),    // Орхидейный
                ColorUtils.rgb(138, 43, 226),     // Синяя орхидея
                ColorUtils.rgb(186, 85, 211),     // Средний фиолетовый
                ColorUtils.rgb(75, 0, 130)        // Индиго
        });
    }

    public Theme() {
        updateTheme();
        HynixMain.getInstance().getEventBus().register(this);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        updateTheme();
    }

    public int getCustom() {
        return Hud.themeColor.getValue();
    }

    public void updateTheme() {
        String currentThemeMode = Hud.themeMode.getValue();
        String currentTheme = Hud.theme.getValue();

        if ("Кастом".equals(currentThemeMode)) {
            setCustomColors();
        } else if ("Шаблон".equals(currentThemeMode)) {
            setTemplateColors(currentTheme);
        }
        else if ("Разноцветный".equals(currentThemeMode)) {
            setRandomColors();
        }
    }
    private void setRandomColors() {
        textColor = ColorUtils.rgb(255, 255, 255); // белый цвет
        mainRectColor = ColorUtils.randomColors().getRGB();
        darkMainRectColor = ColorUtils.randomColors().getRGB();
        rectColor = ColorUtils.randomColors().getRGB();
        darkTextColor = ColorUtils.randomColors().getRGB();
    }
    private void setCustomColors() {
        int customColor = getCustom();
        float brpc = -0.3f;
        textColor = ColorUtils.multDark(customColor, 1.3f + brpc);
        darkTextColor = ColorUtils.multDark(customColor, 1.1f + brpc);
        mainRectColor = ColorUtils.multDark(customColor, 0.9f + brpc);
        darkMainRectColor = ColorUtils.multDark(customColor, 0.5f + brpc);
        rectColor = ColorUtils.multDark(customColor, 1f + brpc);
    }

    private void setTemplateColors(String theme) {
        int[] colors = THEME_COLORS.get(theme);
        if (colors != null) {
            setColors(colors);
        }
    }

    private void setColors(int[] colors) {
        textColor = colors[0];
        darkTextColor = colors[1];
        darkMainRectColor = colors[2];
        mainRectColor = colors[3];
        rectColor = colors[4];
    }
}
