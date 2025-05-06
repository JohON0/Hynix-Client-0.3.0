package io.hynix.ui.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeListSetting;
import io.hynix.managers.theme.Theme;
import io.hynix.utils.client.SoundPlayer;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.HashMap;
import java.util.Map;

import io.hynix.ui.clickgui.components.builder.Component;

public class MultiBoxComponent extends Component {

    final ModeListSetting setting;

    float width = 0;
    float heightPadding = 0;
    float spacing = 3;

    private final Map<BooleanSetting, Animation> animations = new HashMap<>();

    public MultiBoxComponent(ModeListSetting setting) {
        this.setting = setting;
        setHeight(22);
        for (BooleanSetting checkBoxSetting : setting.getValue()) {
            animations.put(checkBoxSetting, new Animation());
        }
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        ClientFonts.tenacityBold[13].drawString(stack, setting.getName(), getX() + 5, getY() + 2, ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        float offset = 0;
        float heightoff = 0;
        boolean anyHovered = false;
        for (BooleanSetting text : setting.getValue()) {
            Animation animation = animations.get(text);
            animation.update();
            animation.animate(text.getValue() ? 1 : 0, 1, Easings.EXPO_OUT);

            int interpolateColor = ColorUtils.interpolateColor(Theme.rectColor, ClickGui.modescolor, (float) animation.getValue());
            int color = ColorUtils.setAlpha(interpolateColor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue()));

            int textColorInterpolate = ColorUtils.interpolateColor(-1, -1, (float) animation.getValue());
            int finalTextColor = ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue()));

            float textWidth = ClientFonts.tenacityBold[13].getWidth(text.getName()) + 3;
            float textHeight = ClientFonts.tenacityBold[13].getFontHeight();
            if (offset + textWidth + spacing >= (getWidth() - 10)) {
                offset = 0;
                heightoff += textHeight + spacing;
            }
            if (MathUtils.isHovered(mouseX, mouseY, getX() + 8 + offset, getY() + 10f + heightoff, textWidth, textHeight)) {
                anyHovered = true;
            }

            RenderUtils.drawShadow(getX() + 7 + offset, getY() + 10 + heightoff, textWidth + 1, textHeight + 2, 2, color);
            RenderUtils.drawRoundedRect(getX() + 7 + offset, getY() + 10 + heightoff, textWidth + 1, textHeight + 2, 2, color);

            ClientFonts.tenacityBold[13].drawString(stack, text.getName(), getX() + 9 + offset, getY() + 13.5f + heightoff, finalTextColor);

            offset += textWidth + spacing;
        }
        width = getWidth() - 15;
        setHeight(22 + heightoff);
        heightPadding = heightoff;
    }

    @Override
    public boolean mouseClick(float mouseX, float mouseY, int mouse) {
        float offset = 0;
        float heightoff = 0;
        for (BooleanSetting text : setting.getValue()) {
            float textWidth = ClientFonts.tenacityBold[13].getWidth(text.getName()) + 3;
            float textHeight = ClientFonts.tenacityBold[13].getFontHeight();
            if (offset + textWidth + spacing >= (getWidth() - 10)) {
                offset = 0;
                heightoff += textHeight + spacing;
            }
            if (mouse == 0 && MathUtils.isHovered(mouseX, mouseY, getX() + 8 + offset, getY() + 10 + heightoff, ClientFonts.tenacityBold[13].getWidth(text.getName()), ClientFonts.tenacityBold[13].getFontHeight() + 1)) {
                text.setValue(!text.getValue());
                SoundPlayer.playSound("guichangemode.wav");
            }
            offset += textWidth + spacing;
        }

        super.mouseClick(mouseX, mouseY, mouse);
        return false;
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}
