package io.hynix.ui.clickgui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RectUtils;
import io.hynix.utils.text.font.ClientFonts;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ConfigButton {
    private int x, y, width, height;
    private String text;
    private boolean isFocused;
    private boolean typing;
    private final String placeholder;

    public ConfigButton(int x, int y, int width, int height, String placeholder) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.placeholder = placeholder;
        this.text = "";
        this.isFocused = false;
        this.typing = false;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        float animation = (float) (1 * io.hynix.ui.clickgui.ClickGui.getGradientAnimation().getValue());
        float posAnimation = 20 - 20 * animation;

        int color = ColorUtils.setAlpha(ClickGui.backgroundColor, (int) (200 * animation));
        RectUtils.getInstance().drawRoundedRectShadowed(matrixStack, x + posAnimation, y, x + width + posAnimation, y + height, 2, 5, color, color, color, color, false, false, true, true);

        ClientFonts.icons_wex[25].drawString(matrixStack, "B", x + 2 + posAnimation, y + (height - 8f) / 2 + 1.5f, ColorUtils.reAlphaInt(ClickGui.textcolor, (int) (255 * animation)));
        ClientFonts.tenacityBold[15].drawString(matrixStack, "Скачать конфиги", x + ClientFonts.icons_wex[25].getWidth("B") + 5 + posAnimation, y + (height - 8f) / 2 + 1.5f, ColorUtils.reAlphaInt(ClickGui.textcolor, (int) (255 * animation)));
    }
}