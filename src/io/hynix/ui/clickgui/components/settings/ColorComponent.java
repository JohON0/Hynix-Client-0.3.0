package io.hynix.ui.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.units.settings.impl.ColorSetting;
import io.hynix.ui.clickgui.components.builder.Component;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.impl.EaseBackIn;
import io.hynix.utils.client.SoundPlayer;
import io.hynix.utils.johon0.math.Vector4i;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;

import java.awt.*;

public class ColorComponent extends Component {

    final ColorSetting setting;

    float colorRectX, colorRectY, colorRectWidth, colorRectHeight;
    float pickerX, pickerY, pickerWidth, pickerHeight;
    float sliderX, sliderY, sliderWidth, sliderHeight;

    final float padding = 5.5f;
    float textX, textY;
    private float[] hsb = new float[2];

    boolean panelOpened;
    boolean draggingHue, draggingPicker;

    final AnimationUtils animation = new EaseBackIn(300, 1, 1);

    public ColorComponent(ColorSetting setting) {
        this.setting = setting;
        hsb = Color.RGBtoHSB(ColorUtils.red(setting.getValue()), ColorUtils.green(setting.getValue()), ColorUtils.blue(setting.getValue()), null);
        setHeight(14);
        animation.setDirection(!panelOpened ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDuration(!panelOpened ? 400 : 300);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        renderTextAndColorRect(stack);

        animation.setDirection(!panelOpened ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDuration(!panelOpened ? 400 : 300);

        GlStateManager.pushMatrix();
        RenderUtils.sizeAnimation(getX() + (getWidth() / 2), (getY() + getHeight() / 2), animation.getOutput());

        if (animation.getOutput() != 0.0) {
            this.setting.setValue(Color.getHSBColor(hsb[0], hsb[1], hsb[2]).getRGB());

            renderSlider(stack, mouseX, mouseY);
            renderPickerPanel(stack, mouseX, mouseY);

            setHeight((float) (20 + (pickerHeight + padding) * animation.getOutput()));
        } else {
            setHeight(14);
        }

        GlStateManager.popMatrix();

        super.render(stack, mouseX, mouseY);
    }

    private void renderTextAndColorRect(MatrixStack stack) {
        String settingName = setting.getName();
        int colorValue = ColorUtils.setAlpha(setting.getValue(), (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue()));

        this.textX = this.getX() + padding;
        this.textY = this.getY() + 3;

        // вывод цвета
        this.colorRectWidth = padding * 3f;
        this.colorRectHeight = padding * 1.5f;
        this.colorRectX = this.getX() + getWidth() - colorRectWidth - padding;
        this.colorRectY = this.getY() + 2;

        // палитра
        this.pickerX = this.getX() + padding;
        this.pickerY = this.getY() + (padding) + 8;
        this.pickerWidth = this.getWidth() - (padding * 2);
        this.pickerHeight = 30;

        // выбор цвета
        this.sliderX = pickerX;
        this.sliderY = pickerY + pickerHeight + padding;
        this.sliderWidth = pickerWidth;
        this.sliderHeight = 3;

        ClientFonts.tenacityBold[14].drawString(stack, settingName, textX, textY, ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        RenderUtils.drawShadow(this.colorRectX + 5, this.colorRectY, this.colorRectWidth-5, this.colorRectHeight, 2, colorValue);
        RenderUtils.drawRoundedRect(this.colorRectX + 5, this.colorRectY, this.colorRectWidth-5, this.colorRectHeight, 2, colorValue);
    }

    private void renderPickerPanel(MatrixStack stack, float mouseX, float mouseY) {
        Vector4i vector4i = new Vector4i(
                ColorUtils.setAlpha(Color.WHITE.getRGB(), (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())),
                ColorUtils.setAlpha(Color.BLACK.getRGB(), (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())),
                ColorUtils.setAlpha(Color.getHSBColor(hsb[0], 1, 1).getRGB(), (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())),
                ColorUtils.setAlpha(Color.BLACK.getRGB(), (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue()))
        );

        float offset = 2;
        float xRange = pickerWidth - offset * 2;
        float yRange = pickerHeight - offset * 2;

        if (draggingPicker) {
            float saturation = MathHelper.clamp((mouseX - pickerX - offset), 0, xRange) / (xRange);
            float brightness = MathHelper.clamp((mouseY - pickerY - offset), 0, yRange) / (yRange);
            hsb[1] = saturation;
            hsb[2] = 1 - brightness;
        }
        RenderUtils.drawRoundedRect(this.pickerX, this.pickerY, this.pickerWidth, this.pickerHeight, new Vector4f(4, 4, 4, 4), vector4i);

        float circleX = pickerX + offset + hsb[1] * (xRange);
        float circleY = pickerY + offset + (1 - hsb[2]) * (yRange);

        RenderUtils.drawCircle(circleX, circleY, 6, ColorUtils.setAlpha(Color.BLACK.getRGB(), (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        RenderUtils.drawCircle(circleX, circleY, 4, ColorUtils.setAlpha(Color.WHITE.getRGB(), (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
    }


    private void renderSlider(MatrixStack stack, float mouseX, float mouseY) {
        float slH = 3;
        for (int i = 0; i < sliderWidth; i++) {
            float hue = i / sliderWidth;
            RenderUtils.drawRoundedRect(this.sliderX + i, sliderY - slH / 2f, slH, sliderHeight, 1, ColorUtils.setAlpha(Color.HSBtoRGB(hue, 1, 1), (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));

        }
        RenderUtils.drawCircle(this.sliderX + (hsb[0] * sliderWidth), this.sliderY, 6, ColorUtils.rgba(0, 0, 0, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        RenderUtils.drawCircle(this.sliderX + (hsb[0] * sliderWidth), this.sliderY, 4, ColorUtils.setAlpha(-1, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        if (draggingHue) {
            float hue = (mouseX - sliderX) / sliderWidth;
            hsb[0] = MathHelper.clamp(hue, 0,1);
        }
    }

    @Override
    public boolean mouseClick(float mouseX, float mouseY, int mouse) {
        if (RenderUtils.isInRegion(mouseX, mouseY, colorRectX, colorRectY, colorRectWidth, colorRectHeight)) {
            panelOpened = !panelOpened;
            SoundPlayer.playSound(panelOpened ? "guicoloropen.wav" : "guicolorclose.wav", 0.25);
        }

        if (panelOpened && mouse == 0) {
            if (RenderUtils.isInRegion(mouseX, mouseY, sliderX, sliderY - 1.5f, sliderWidth, sliderHeight)) {
                draggingHue = true;
            } else if (RenderUtils.isInRegion(mouseX, mouseY, pickerX, pickerY, pickerWidth, pickerHeight) && animation.isDone()) {
                draggingPicker = true;
            }
        }

        super.mouseClick(mouseX, mouseY, mouse);
        return false;
    }


    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        if (mouse == 0) {
            if (draggingHue) {
                SoundPlayer.playSound("guicolorselect.wav", 0.25);
                draggingHue = false;
            }
            if (draggingPicker) {
                SoundPlayer.playSound("guicolorselect.wav", 0.25);
                draggingPicker = false;
            }
        }
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}
