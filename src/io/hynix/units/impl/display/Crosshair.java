package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.EventRender2D;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.managers.theme.Theme;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import lombok.Getter;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.math.RayTraceResult.Type;

import java.awt.*;

@UnitRegister(name = "Crosshair", category = Category.Display, desc = "Кастом прицел")
public class Crosshair extends Unit {

    public final ModeSetting mode = new ModeSetting("Вид", "Орбиз", "Орбиз", "Класический", "Круг");

    public final BooleanSetting staticCrosshair = new BooleanSetting("Статический", false).setVisible(() -> mode.is("Орбиз"));
    private float lastYaw;
    private float lastPitch;

    @Getter
    public float animatedYaw, x;
    @Getter
    public float animatedPitch, y;

    private float animation;
    private float animationSize;

    private final int outlineColor = Color.BLACK.getRGB();
    private final int entityColor = Color.RED.getRGB();

    public Crosshair() {
        addSettings(mode, staticCrosshair);
    }

    @Subscribe
    public void onDisplay(EventRender2D e) {
        if (mc.player == null || mc.world == null || e.getType() != EventRender2D.Type.POST) {
            return;
        }

        x = mc.getMainWindow().getScaledWidth() / 2f;
        y = mc.getMainWindow().getScaledHeight() / 2f;

        switch (mode.getIndex()) {
            case 0 -> {
                float size = 5;

                animatedYaw = MathUtils.fast(animatedYaw, ((lastYaw - mc.player.rotationYaw) + mc.player.moveStrafing) * size, 5);
                animatedPitch = MathUtils.fast(animatedPitch, ((lastPitch - mc.player.rotationPitch) + mc.player.moveForward) * size, 5);
                animation = MathUtils.fast(animation, mc.objectMouseOver.getType() == Type.ENTITY ? 1 : 0, 5);

                int color = ColorUtils.interpolate(Theme.rectColor, Theme.mainRectColor, 1 - animation);

                if (!staticCrosshair.getValue()) {
                    x += animatedYaw;
                    y += animatedPitch;
                }

                animationSize = MathUtils.fast(animationSize, (1 - mc.player.getCooledAttackStrength(1)) * 3, 10);

                float radius = 3 + (staticCrosshair.getValue() ? 0 : animationSize);
                if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                    RenderUtils.drawShadowCircle(x, y, radius * 2, ColorUtils.setAlpha(color, 64));
                    RenderUtils.drawCircle(x, y, radius, color);
                }
                lastYaw = mc.player.rotationYaw;
                lastPitch = mc.player.rotationPitch;
            }

            case 1 -> {
                if (mc.gameSettings.getPointOfView() != PointOfView.FIRST_PERSON) return;

                float cooldown = 1 - mc.player.getCooledAttackStrength(0);
                animationSize = MathUtils.fast(animationSize, (1 - mc.player.getCooledAttackStrength(0)), 10);
                float thickness = 1;
                float length = 3;
                float gap = 2 + 8 * animationSize;

                int color = mc.pointedEntity != null ? entityColor : -1;

                drawOutlined(x - thickness / 2, y - gap - length, thickness, length, color);
                drawOutlined(x - thickness / 2, y + gap, thickness, length, color);

                drawOutlined(x - gap - length, y - thickness / 2, length, thickness, color);
                drawOutlined(x + gap, y - thickness / 2, length, thickness, color);
            }

            case 2 -> {
                animationSize = MathUtils.fast(animationSize, (1 - mc.player.getCooledAttackStrength(1)) * 260, 10);
                if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                    RenderUtils.drawCircleWithFill(x, y, 0, 360, 3.8f, 3, false, ColorUtils.rgb(23,21,21));
                    RenderUtils.drawCircleWithFill(x, y, animationSize, 360, 3.8f, 3, false, ColorUtils.getColor1(Theme.rectColor, Theme.mainRectColor, 16, 0));
                }
            }
        }
    }

    private void drawOutlined(
            final float x,
            final float y,
            final float w,
            final float h,
            final int hex
    ) {
        RenderUtils.drawRectW(x - 0.5, y - 0.5, w + 1, h + 1, outlineColor);
        RenderUtils.drawRectW(x, y, w, h, hex);
    }
}
