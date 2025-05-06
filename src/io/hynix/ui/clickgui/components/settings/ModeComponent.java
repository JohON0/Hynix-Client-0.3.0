package io.hynix.ui.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.managers.theme.Theme;
import io.hynix.utils.client.SoundPlayer;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import io.hynix.ui.clickgui.components.builder.Component;

import java.util.HashMap;
import java.util.Map;

public class ModeComponent extends Component {

    final ModeSetting setting;

    float width = 0;
    float heightplus = 0;
    float spacing = 5;

    private final Map<String, Animation> animations = new HashMap<>();

    public ModeComponent(ModeSetting setting) {
        this.setting = setting;
        setHeight(22);
        for (String option : setting.strings) {
            animations.put(option, new Animation());
        }
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        spacing = 5;
        ClientFonts.tenacityBold[14].drawString(stack, setting.getName() + ":", getX() + 5, getY() + 2, ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));

        float offset = 0;
        float heightoff = 0;
        boolean anyHovered = false;

        for (String text : setting.strings) {
            Animation animation = animations.get(text);
            animation.update();
            animation.animate(text.equals(setting.getValue()) ? 1 : 0, 1, Easings.EXPO_OUT);
            float textWidth = ClientFonts.tenacityBold[13].getWidth(text) + 3;
            float textHeight = ClientFonts.tenacityBold[13].getFontHeight();

            if (offset + textWidth + spacing >= (getWidth() - 10)) {
                offset = 0;
                heightoff += textHeight + spacing / 2;
            }
            if (MathUtils.isHovered(mouseX, mouseY, getX() + 8 + offset, getY() + 11.5f + heightoff, textWidth, textHeight)) {
                anyHovered = true;
            }

            int interpolateColor = ColorUtils.interpolateColor(Theme.rectColor, ClickGui.modescolor, (float) animation.getValue());
            int color = ColorUtils.setAlpha(interpolateColor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue()));

            RenderUtils.drawShadow(getX() + 7 + offset, getY() + 10 + heightoff, textWidth + 1, textHeight + 2, 2, color);
            RenderUtils.drawRoundedRect(getX() + 7 + offset, getY() + 10 + heightoff, textWidth + 1, textHeight + 2, 2, color);

            ClientFonts.tenacityBold[13].drawString(stack, text, getX() + 9 + offset, getY() + 13.5f + heightoff, ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));

            offset += textWidth + spacing / 2;
        }


        width = getWidth() - 15;
        setHeight(22 + heightoff);
        heightplus = heightoff;
    }

    @Override
    public boolean mouseClick(float mouseX, float mouseY, int mouse) {
        float offset = 0;
        float heightoff = 0;
        for (String text : setting.strings) {
            float textWidth = ClientFonts.tenacityBold[13].getWidth(text) + 3;
            float textHeight = ClientFonts.tenacityBold[13].getFontHeight();

            if (offset + textWidth + spacing >= (getWidth() - 10)) {
                offset = 0;
                heightoff += textHeight + spacing / 2;
            }
            if (mouse == 0 && !text.equals(setting.getValue()) && MathUtils.isHovered(mouseX, mouseY, getX() + 8 + offset, getY() + 10 + heightoff, ClientFonts.tenacityBold[13].getWidth(text), ClientFonts.tenacityBold[13].getFontHeight())) {
                setting.setValue(text);
                SoundPlayer.playSound("guichangemode.wav");
            }
            offset += textWidth + spacing / 2;
        }

        super.mouseClick(mouseX, mouseY, mouse);
        return false;
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}