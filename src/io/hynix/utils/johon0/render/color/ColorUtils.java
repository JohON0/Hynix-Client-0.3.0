package io.hynix.utils.johon0.render.color;

import io.hynix.managers.theme.Theme;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;

import com.mojang.blaze3d.systems.RenderSystem;

import io.hynix.utils.johon0.math.MathUtils;

import java.awt.Color;

@UtilityClass
public class ColorUtils {
    private Color targetColor;
    private Color currentColor;
    private long lastUpdateTime;
    private static final long INTERVAL = 5000; // интервал обновления в миллисекундах
    private static final float BLEND_RATIO = 1F; // скорость изменения (меньше - быстрее изменение)
    public final int green = new Color(64, 255, 64).getRGB();
    public final int yellow = new Color(255, 255, 64).getRGB();
    public final int orange = new Color(255, 128, 32).getRGB();
    public final int red = new Color(255, 64, 64).getRGB();

    public static int rgb(int r, int g, int b) {
        return 255 << 24 | r << 16 | g << 8 | b;
    }

    public int multDark(int c, float brpc) {
        return getColor((float) red(c) * brpc, (float) green(c) * brpc, (float) blue(c) * brpc, (float) alpha(c));
    }

    public static float[] getRGBAf(int c) {
        return new float[]{(float) red(c) / 255.F, (float) green(c) / 255.F, (float) blue(c) / 255.F, (float) alpha(c) / 255.F};
    }

    public int getColor() {
        return getColor(1);
    }

    public int getColor(int index) {
        return gradient(Theme.darkMainRectColor, Theme.darkMainRectColor, index * 16, 10);
    }

    public static class IntColor {

        public static float[] rgb(final int color) {
            return new float[]{
                    (color >> 16 & 0xFF) / 255f,
                    (color >> 8 & 0xFF) / 255f,
                    (color & 0xFF) / 255f,
                    (color >> 24 & 0xFF) / 255f
            };
        }

        public static int rgba(final int r,
                               final int g,
                               final int b,
                               final int a) {
            return a << 24 | r << 16 | g << 8 | b;
        }

        public static int rgb(int r, int g, int b) {
            return 255 << 24 | r << 16 | g << 8 | b;
        }


        public static int getRed(final int hex) {
            return hex >> 16 & 255;
        }

        public static int getGreen(final int hex) {
            return hex >> 8 & 255;
        }

        public static int getBlue(final int hex) {
            return hex & 255;
        }

        public static int getAlpha(final int hex) {
            return hex >> 24 & 255;
        }
    }

    public Color random() {
        return new Color(Color.HSBtoRGB((float) Math.random(), (float) (0.75F + (Math.random() / 4F)), (float) (0.75F + (Math.random() / 4F))));
    }
    public Color randomColors() {
        long currentTime = System.currentTimeMillis();
        lastUpdateTime = System.currentTimeMillis(); // Установка времени последнего обновления
        currentColor = random(); // Инициализация текущего цвета
        targetColor = random(); // Инициализация целевого цвета

        // Если интервал обновления истек, генерируем новый целевой цвет
        if (currentTime - lastUpdateTime >= INTERVAL) {
            targetColor = random(); // Генерация нового целевого цвета
            lastUpdateTime = currentTime;
        }

        currentColor = blendColors(currentColor, targetColor, 0.99f);
        return currentColor;
    }

    private Color blendColors(Color color1, Color color2, float ratio) {
        int r = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * ratio);
        int g = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * ratio);
        int b = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * ratio);
        return new Color(r, g, b);
    }
    public int overCol(int c1, int c2, float pc01) {
        return getColor((float) red(c1) * (1 - pc01) + (float) red(c2) * pc01, (float) green(c1) * (1 - pc01) + (float) green(c2) * pc01, (float) blue(c1) * (1 - pc01) + (float) blue(c2) * pc01, (float) alpha(c1) * (1 - pc01) + (float) alpha(c2) * pc01);
    }

    public int overCol(int c1, int c2) {
        return overCol(c1, c2, 0.5f);
    }

    public static int rgba(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static void setAlphaColor(final int color, final float alpha) {
        final float red = (float) (color >> 16 & 255) / 255.0F;
        final float green = (float) (color >> 8 & 255) / 255.0F;
        final float blue = (float) (color & 255) / 255.0F;
        RenderSystem.color4f(red, green, blue, alpha);
    }

    public int red(int c) {
        return c >> 16 & 0xFF;
    }

    public int green(int c) {
        return c >> 8 & 0xFF;
    }

    public int blue(int c) {
        return c & 0xFF;
    }

    public int alpha(int c) {
        return c >> 24 & 0xFF;
    }

    public float redf(int c) {
        return (float) red(c) / 255.F;
    }

    public float greenf(int c) {
        return (float) green(c) / 255.F;
    }

    public float bluef(int c) {
        return (float) blue(c) / 255.F;
    }

    public float alphaf(int c) {
        return (float) alpha(c) / 255.F;
    }

    public static void setColor(int color) {
        setAlphaColor(color, (float) (color >> 24 & 255) / 255.0F);
    }

    public static int toColor(String hexColor) {
        int argb = Integer.parseInt(hexColor.substring(1), 16);
        return setAlpha(argb, 255);
    }
    public static int setAlpha(int color, int alpha) {
        return (color & 0x00ffffff) | (alpha << 24);
    }

    public static float[] rgba(final int color) {
        return new float[] {
                (color >> 16 & 0xFF) / 255f,
                (color >> 8 & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                (color >> 24 & 0xFF) / 255f
        };
    }

    public static int reAlphaInt(final int color,
                                 final int alpha) {
        return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 16777215);
    }

    public int getColor(double d, double e, double f, double g) {
        return new Color((int) d, (int) e, (int) f, (int) g).getRGB();
    }

    public int getColor(float r, float g, float b, float a) {
        return new Color((int) r, (int) g, (int) b, (int) a).getRGB();
    }

    public int multAlpha(int c, float apc) {
        return getColor(red(c), green(c), blue(c), (float) alpha(c) * apc);
    }

    public int astolfo(int speed, int index) {
        double angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        return Color.getHSBColor(
                ((angle %= 360) / 360.0) < 0.5 ? -((float) (angle / 360.0)) : (float) (angle / 360.0),
                0.5F,
                1.0F
        ).hashCode();
    }

    public int rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        int color = Color.HSBtoRGB(hue, saturation, brightness);
        return getColor(
                red(color),
                green(color),
                blue(color),
                Math.max(0, Math.min(255, (int) (opacity * 255)))
        );
    }

    public static int getOverallColorFrom(int color1, int color2, float percentTo2) {
        final int finalRed = (int) MathUtils.lerp(color1 >> 16 & 0xFF, color2 >> 16 & 0xFF, percentTo2),
                finalGreen = (int) MathUtils.lerp(color1 >> 8 & 0xFF, color2 >> 8 & 0xFF, percentTo2),
                finalBlue = (int) MathUtils.lerp(color1 & 0xFF, color2 & 0xFF, percentTo2),
                finalAlpha = (int) MathUtils.lerp(color1 >> 24 & 0xFF, color2 >> 24 & 0xFF, percentTo2);
        return getColor(finalRed, finalGreen, finalBlue, finalAlpha);
    }

    public static int gradient(int start, int end, int index, int speed) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;
        int color = interpolate(start, end, MathHelper.clamp(angle / 180f - 1, 0, 1));
        float[] hs = rgba(color);
        float[] hsb = Color.RGBtoHSB((int) (hs[0] * 255), (int) (hs[1] * 255), (int) (hs[2] * 255), null);

        hsb[1] *= 1.5F;
        hsb[1] = Math.min(hsb[1], 1.0f);

        return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }

    public Color interpolate(Color color1, Color color2, double amount) {
        amount = 1F - amount;
        amount = (float) MathHelper.clamp(0, 1, amount);
        return new Color(
                (int) MathUtils.lerp(color1.getRed(), color2.getRed(), amount),
                (int) MathUtils.lerp(color1.getGreen(), color2.getGreen(), amount),
                (int) MathUtils.lerp(color1.getBlue(), color2.getBlue(), amount),
                (int) MathUtils.lerp(color1.getAlpha(), color2.getAlpha(), amount)
        );
    }

    public int getColor(int r, int g, int b, int a) {
        return new Color(r, g, b, a).getRGB();
    }

    public int getColor(int r, int g, int b) {
        return new Color(r, g, b, 255).getRGB();
    }

    public int getColor(int br, int a) {
        return new Color(br, br, br, a).getRGB();
    }

    public int interpolate(int color1, int color2, double amount) {
        amount = (float) MathHelper.clamp(0, 1, amount);
        return getColor(
                MathUtils.lerp(red(color1), red(color2), amount),
                MathUtils.lerp(green(color1), green(color2), amount),
                MathUtils.lerp(blue(color1), blue(color2), amount),
                MathUtils.lerp(alpha(color1), alpha(color2), amount)
        );
    }

    public static int interpolateColor(int to, int from, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        int red1 = red(to);
        int green1 = green(to);
        int blue1 = blue(to);
        int alpha1 = alpha(to);
        int red2 = red(from);
        int green2 = green(from);
        int blue2 = blue(from);
        int alpha2 = alpha(from);
        int interpolatedRed = interpolateInt(red1, red2, amount);
        int interpolatedGreen = interpolateInt(green1, green2, amount);
        int interpolatedBlue = interpolateInt(blue1, blue2, amount);
        int interpolatedAlpha = interpolateInt(alpha1, alpha2, amount);
        return interpolatedAlpha << 24 | interpolatedRed << 16 | interpolatedGreen << 8 | interpolatedBlue;
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float)interpolationValue);
    }

    public static int interpolate(int start, int end, float value) {
        float[] startColor = rgba(start);
        float[] endColor = rgba(end);

        return rgba((int) MathUtils.interpolate(startColor[0] * 255, endColor[0] * 255, value),
                (int) MathUtils.interpolate(startColor[1] * 255, endColor[1] * 255, value),
                (int) MathUtils.interpolate(startColor[2] * 255, endColor[2] * 255, value),
                (int) MathUtils.interpolate(startColor[3] * 255, endColor[3] * 255, value));
    }
    public static int getRed(final int hex) {
        return hex >> 16 & 255;
    }

    public static int getGreen(final int hex) {
        return hex >> 8 & 255;
    }

    public static int getBlue(final int hex) {
        return hex & 255;
    }

    public static int getAlpha(final int hex) {
        return hex >> 24 & 255;
    }
    public static int interpolateColors(int color1, int color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        int red1 = getRed(color1);
        int green1 = getGreen(color1);
        int blue1 = getBlue(color1);
        int alpha1 = getAlpha(color1);

        int red2 = getRed(color2);
        int green2 = getGreen(color2);
        int blue2 = getBlue(color2);
        int alpha2 = getAlpha(color2);

        int interpolatedRed = interpolateInt(red1, red2, amount);
        int interpolatedGreen = interpolateInt(green1, green2, amount);
        int interpolatedBlue = interpolateInt(blue1, blue2, amount);
        int interpolatedAlpha = interpolateInt(alpha1, alpha2, amount);

        return (interpolatedAlpha << 24) | (interpolatedRed << 16) | (interpolatedGreen << 8) | interpolatedBlue;
    }

    public static int getColor1(int firstColor, int secondColor, int index, float mult) {
        return ColorUtils.gradient(firstColor, secondColor, (int) (index * mult), 10);
    }
    public static Color lerp(int speed, int index, Color start, Color end) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return interpolate(start, end, angle / 360f);
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) MathHelper.clamp(0, 255, alpha));

    }

}
