package io.hynix.ui.notifications.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.ui.hud.impl.NotificationRender;
import io.hynix.ui.notifications.Notification;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import net.minecraft.client.gui.screen.ChatScreen;

public class SuccessNotify extends Notification {

    public SuccessNotify(String content, long delay) {
        super(content, delay);
        animationY.setValue(window.getScaledHeight());
        alphaAnimation.setValue(0);
    }

    @Override
    public void render(MatrixStack matrixStack, int multiplierY) {
        int fontSize = 14;
        int iconSizeF = 24;
        this.end = ((this.getInit() + this.getDelay()) - System.currentTimeMillis()) <= (this.getDelay() - 500) - this.getDelay();
        if (mc.currentScreen instanceof ChatScreen) {
            chatOffset.run(ClientFonts.tenacityBold[fontSize].getFontHeight() + 32);
        } else {
            chatOffset.run(ClientFonts.tenacityBold[fontSize].getFontHeight() + 2);
        }

        float contentWidth = ClientFonts.tenacityBold[fontSize].getWidth(getContent());

        float x, y, width, height;
        float iconSize = ClientFonts.icons_wex[iconSizeF].getWidth("K");

        width = margin + contentWidth + margin;
        height = (margin / 2F) + ClientFonts.tenacityBold[fontSize].getFontHeight() + (margin / 2F);

        x = (float) NotificationRender.posX;
        if (NotificationRender.posY <= mc.getMainWindow().scaledHeight() / 2f || NotificationRender.posY == mc.getMainWindow().scaledHeight() / 2f - 30) {
            y = (float) NotificationRender.posY + (height * multiplierY) + (multiplierY * 4);
        } else {
            y = (float) NotificationRender.posY - (height * multiplierY) - (multiplierY * 4);
        }
        alphaAnimation.run(this.end ? 0 : 1);
        animationY.run(this.end ? y + 1 : y);

        float posX = x;
        float posY = (float) animationY.getValue();

        double alphaValue = alphaAnimation.getValue();
        RenderUtils.drawShadow(posX - iconSize, posY, width + iconSize, height, 4, ColorUtils.reAlphaInt(ClickGui.backgroundColor, (int) (100 * alphaValue)));
        RenderUtils.drawRoundedRect(posX - iconSize, posY, width + iconSize, height, 4, ColorUtils.reAlphaInt(ClickGui.backgroundColor, (int) (100 * alphaValue)));
        ClientFonts.icons_wex[iconSizeF].drawString(matrixStack, "K", posX - (iconSize - 2.5), posY - height / 2 + ClientFonts.icons_wex[iconSizeF].getFontHeight() - margin / 2 + 2.5f, ColorUtils.reAlphaInt(ClickGui.textcolor, (int) (255 * alphaValue)));
        ClientFonts.tenacityBold[fontSize].drawString(matrixStack, getContent(), posX + margin, posY + margin / 2F + 3f, ColorUtils.reAlphaInt(ClickGui.textcolor, (int) (255 * alphaAnimation.getValue())));
    }

    @Override
    public boolean hasExpired() {
        return this.animationY.isFinished() && this.end;
    }
}
