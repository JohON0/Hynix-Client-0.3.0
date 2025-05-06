package io.hynix.ui.hud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.mojang.blaze3d.platform.GlStateManager;
import io.hynix.events.impl.EventRender2D;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.ui.hud.updater.ElementRenderer;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.easing.CompactAnimation;
import io.hynix.utils.johon0.animations.easing.Easing;
import io.hynix.utils.johon0.animations.impl.EaseBackIn;
import io.hynix.managers.drag.Dragging;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.text.TextFormatting;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class NotificationRender implements ElementRenderer {

    final Dragging dragging;
    private final CompactAnimation widthAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    private final CompactAnimation heightAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    double width;
    float height;
    public static float posX;
    public static float posY;
    final AnimationUtils animation = new EaseBackIn(300, 1, 1);
    private int currentSymbolIndex = 0;
    private final String[] symbols = {"I", "F", "G"};
    private long lastUpdateTime = 0;
    private final long updateDelay = 500;

    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack ms = eventRender2D.getMatrixStack();
        float padding = 5;
        int fontSize = 14;
        int iconSizeF = 24;
        float margin = 2f;
        float iconSize = ClientFonts.icons_wex[iconSizeF].getWidth("L");
        posX = dragging.getX();
        posY = dragging.getY();
        boolean isAnyModuleEnabled = false;

        if (mc.currentScreen instanceof ChatScreen) {
            isAnyModuleEnabled = true;
        }

        animation.setDirection(isAnyModuleEnabled ? Direction.FORWARDS : Direction.BACKWARDS);
        animation.setDuration(isAnyModuleEnabled ? 300 : 200);


        GlStateManager.pushMatrix();
        RenderUtils.sizeAnimation(posX - iconSize, posY, animation.getOutput());
        RenderUtils.drawShadow(posX - iconSize, posY, (float) (width + iconSize), height, 4, ClickGui.backgroundColor);
        RenderUtils.drawRoundedRect(posX - iconSize, posY, (float) (width + iconSize), height, 4, ClickGui.backgroundColor);

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastUpdateTime >= updateDelay) {
            currentSymbolIndex = (currentSymbolIndex + 1) % symbols.length;
            lastUpdateTime = currentTime;
        }

        ClientFonts.icons_wex[iconSizeF].drawString(ms, symbols[currentSymbolIndex], posX - (iconSize - 2.5), posY - height / 2 + ClientFonts.icons_wex[iconSizeF].getFontHeight() - margin / 2 + 3f, ColorUtils.reAlphaInt(ClickGui.textcolor, (int) (255)));
        ClientFonts.tenacityBold[fontSize].drawString(ms, "Меня можно" + TextFormatting.GREEN + " двигать =)", posX + margin + 2, posY + margin / 2F + 4.5f, ColorUtils.reAlphaInt(ClickGui.textcolor, (int) (255)));
        float maxWidth = ClientFonts.tenacityBold[fontSize].getWidth("Меня можно" + " двигать =)") + 7;
        float localHeight = fontSize;

        GlStateManager.popMatrix();

        widthAnimation.run(Math.max(maxWidth,iconSize + 25));
        width = widthAnimation.getValue();
        heightAnimation.run((localHeight + 5.5));
        height = (float) heightAnimation.getValue() - 5;
        dragging.setWidth((float) width);
        dragging.setHeight(height);
    }
}
