package io.hynix.ui.clickgui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.other.Scissor;
import io.hynix.utils.johon0.render.render2d.RectUtils;
import io.hynix.utils.text.font.ClientFonts;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WaterMark {
    private int x, y, width, height;
    private String text;
    private boolean isFocused;
    private boolean typing;
    private final String placeholder;

    public WaterMark(int x, int y, int width, int height, String placeholder) {
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
        RectUtils.getInstance().drawRoundedRectShadowed(matrixStack, x, y, x + width , y + height + posAnimation, 2, 5, color, color, color, color, false, false, true, true);

        Scissor.push();
        Scissor.setFromComponentCoordinates(x - posAnimation, y, width , height + posAnimation);
        ClientFonts.dev[40].drawString(matrixStack, "F", x + 2, y + (height - 8f) / 2 - 5 + posAnimation, ColorUtils.reAlphaInt(ClickGui.textcolor, (int) (255 * animation)));
        ClientFonts.tenacityBold[15].drawString(matrixStack, "Hynix Free Edition", x + ClientFonts.icons_wex[25].getWidth("B") + 5 , y + (height - 8f) / 2 + 1.5f+ posAnimation, ColorUtils.reAlphaInt(ClickGui.textcolor, (int) (255 * animation)));
        Scissor.unset();
        Scissor.pop();
    }
}