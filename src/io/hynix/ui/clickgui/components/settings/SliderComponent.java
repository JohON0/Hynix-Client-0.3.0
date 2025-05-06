package io.hynix.ui.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.managers.theme.Theme;
import io.hynix.utils.client.SoundPlayer;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.johon0.render.font.Fonts;
import io.hynix.utils.text.font.ClientFonts;
import net.minecraft.util.math.MathHelper;
import io.hynix.ui.clickgui.components.builder.Component;

/**
 * SliderComponent
 */
public class SliderComponent extends Component {

    private final SliderSetting setting;

    public SliderComponent(SliderSetting setting) {
        this.setting = setting;
        this.setHeight(18);
    }
    private float newValue, lastValue;
    private float anim;
    private boolean drag;
    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        ClientFonts.tenacityBold[14].drawString(stack, setting.getName(), getX() + 5, getY() + 4.5f / 2f + 1, ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        ClientFonts.tenacityBold[14].drawString(stack, String.valueOf(setting.getValue()), getX() + getWidth() - 5 - Fonts.montserrat.getWidth( String.valueOf(setting.getValue()), 5.5f), getY() + 4.5f / 2f + 1, ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));

        RenderUtils.drawRoundedRect(getX() + 5, getY() + 11, getWidth() - 10, 3, 1, ColorUtils.setAlpha(ClickGui.lightcolorgui, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        anim = MathUtils.fast(anim, (getWidth() - 10) * (setting.getValue() - setting.min) / (setting.max - setting.min), 20);
        float sliderWidth = anim;
        RenderUtils.drawRoundedRect(getX() + 5, getY() + 11, sliderWidth, 3, 1, ColorUtils.setAlpha(Theme.rectColor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        RenderUtils.drawCircle(getX() + 5 + sliderWidth, getY() + 12.5f, 6, ColorUtils.setAlpha(Theme.rectColor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        if (drag) {
            float newValue = (float) MathHelper.clamp(
                    MathUtils.round((mouseX - getX() - 5) / (getWidth() - 10) * (setting.max - setting.min) + setting.min,
                            setting.increment), setting.min, setting.max);
            if (newValue != lastValue) {
                setting.setValue(newValue);
                lastValue = newValue;
                SoundPlayer.playSound("guislidermove.wav");
            }
        }
    }

    @Override
    public boolean mouseClick(float mouseX, float mouseY, int mouse) {
        // TODO Auto-generated method stub
        if (MathUtils.isHovered(mouseX, mouseY, getX() + 5, getY() + 11, getWidth() - 10, 4)) {
            drag = true;
        }

        super.mouseClick(mouseX, mouseY, mouse);
        return false;
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        // TODO Auto-generated method stub
        drag = false;
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}