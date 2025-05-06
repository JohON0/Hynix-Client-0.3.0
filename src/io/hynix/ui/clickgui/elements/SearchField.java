package io.hynix.ui.clickgui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.other.Scissor;
import io.hynix.utils.johon0.render.render2d.RectUtils;
import io.hynix.utils.text.font.ClientFonts;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;

@Setter
@Getter
public class SearchField {
    private int x, y, width, height;
    private String text;
    private boolean isFocused;
    public boolean typing;
    private final String placeholder;

    public SearchField(int x, int y, int width, int height, String placeholder) {
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
        String textToDraw = text.isEmpty() && !typing ? placeholder : text;
        String cursor = typing && System.currentTimeMillis() % 1000 > 500 ? "_" : "";
        int color = ColorUtils.setAlpha(ClickGui.backgroundColor, (int) (200 * animation));

        RectUtils.getInstance().drawRoundedRectShadowed(matrixStack, x - posAnimation, y, x + width - posAnimation, y + height, 2, 5, color, color, color, color, false, false, true, true);

        Scissor.push();
        Scissor.setFromComponentCoordinates(x - posAnimation, y, width + posAnimation, height);
        ClientFonts.icon[20].drawString(matrixStack, "a", x + 3 - posAnimation, y + (height - 8f) / 2 + 1.5f, ColorUtils.reAlphaInt(ClickGui.textcolor, (int) (255 * animation)));

        ClientFonts.tenacityBold[15].drawString(matrixStack, textToDraw + cursor, x + 8 + ClientFonts.icon[15].getWidth("a") - posAnimation, y + (height - 8f) / 2 + 1.5f, ColorUtils.reAlphaInt(ClickGui.textcolor, (int) (255 * animation)));
        Scissor.unset();
        Scissor.pop();
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (typing) {
            text += codePoint;
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ClientUtils.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            text = "";
        }
        if (typing && keyCode == GLFW.GLFW_KEY_BACKSPACE && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            typing = false;
        }
        if (ClientUtils.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_V) {
            text += ClientUtils.pasteString();
        }
        if (ClientUtils.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_F) {
            typing = true;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!MathUtils.isHovered((float) mouseX, (float) mouseY, x, y, width, height)){
            isFocused = false;
        }
        isFocused = MathUtils.isHovered((float) mouseX, (float) mouseY, x, y, width, height);
        typing = isFocused;
        return isFocused;
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }
    public void setFocused(boolean focused) { isFocused = focused; }
}
