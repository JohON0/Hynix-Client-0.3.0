package io.hynix.ui.hud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.mojang.blaze3d.platform.GlStateManager;
import io.hynix.HynixMain;
import io.hynix.events.impl.EventRender2D;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.units.api.Unit;
import io.hynix.ui.hud.updater.ElementRenderer;
import io.hynix.managers.theme.Theme;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.easing.CompactAnimation;
import io.hynix.utils.johon0.animations.easing.Easing;
import io.hynix.utils.johon0.animations.impl.EaseBackIn;
import io.hynix.utils.client.KeyStorage;
import io.hynix.managers.drag.Dragging;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.johon0.render.other.Scissor;
import io.hynix.utils.johon0.render.font.Fonts;
import io.hynix.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ChatScreen;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Keybinds implements ElementRenderer {

    final Dragging dragging;
    private final CompactAnimation widthAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    private final CompactAnimation heightAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    double width;
    float height;
    final AnimationUtils animation = new EaseBackIn(300, 1, 1);

    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack ms = eventRender2D.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 6.5f;
        float padding = 5;
        float iconSize = 10;
        float margin = 2f;
        String name = "Keybinds";
        float nameWidth = Fonts.montserrat.getWidth(name, fontSize, 0.07f);
        boolean isAnyModuleEnabled = false;


        for (Unit f : HynixMain.getInstance().getModuleManager().getModules()) {
            f.getAnimation().update();
            if (f.getAnimation().getValue() > 0.1 && f.getBind() != 0 || mc.currentScreen instanceof ChatScreen) {
                isAnyModuleEnabled = true;
                break;
            }
        }

        animation.setDirection(isAnyModuleEnabled ? Direction.FORWARDS : Direction.BACKWARDS);
        animation.setDuration(isAnyModuleEnabled ? 300 : 200);

        GlStateManager.pushMatrix();
        RenderUtils.sizeAnimation(posX + (width / 2), (posY + height / 2), animation.getOutput());
        RenderUtils.drawShadow(posX,posY, (float) width, height, 5, ClickGui.backgroundColor);
        RenderUtils.drawRoundedRect(posX,posY, (float) width, height, 4, ClickGui.backgroundColor);
//        Fonts.montserrat.drawText(ms, name, posX + iconSize + 7, posY + padding + 0.5f, Color.WHITE.getRGB(), fontSize, 0.07f);
        ClientFonts.tenacity[16].drawString(ms, name, posX + iconSize, posY + padding + 4, ClickGui.textcolor);
        ClientFonts.dev[25].drawString(ms, "D", posX + width - padding - 10, posY + 6f, Theme.rectColor);

        float maxWidth = ClientFonts.tenacityBold[14].getWidth(name) + padding * 2;
        float localHeight = fontSize + padding * 2;
        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);
        posY += fontSize + padding + 2f;
        posY += 7f;
        for (Unit f : HynixMain.getInstance().getModuleManager().getModules()) {
            f.getAnimation().update();
            int color = ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * f.getAnimation().getValue()));
            if (!(f.getAnimation().getValue() > 0.1) || f.getBind() == 0) continue;
            String nameText = f.getName();
            float moduleWidth = ClientFonts.tenacityBold[14].getWidth(nameText);

            String bindText = KeyStorage.getKey(f.getBind());
            float bindWidth = ClientFonts.tenacityBold[14].getWidth(bindText);

            float localWidth = moduleWidth + bindWidth + padding * 3;
            ClientFonts.tenacityBold[14].drawString(ms, nameText, (float) (posX + padding - 0.5f - 4 + 4 * f.getAnimation().getValue()), (float) (posY + 3.5 - 3 * f.getAnimation().getValue()), color);
            ClientFonts.tenacityBold[14].drawString(ms, bindText, (float) (posX + 1 + width - padding - bindWidth * f.getAnimation().getValue()), (float) (posY + 4 - 3 * f.getAnimation().getValue()), color);

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += (float) ((fontSize + padding - 3f) * f.getAnimation().getValue());
            localHeight += (float) (fontSize + padding - 3f);
        }
        Scissor.unset();
        Scissor.pop();

        GlStateManager.popMatrix();

        widthAnimation.run(Math.max(maxWidth, nameWidth + iconSize + 25));
        width = widthAnimation.getValue();
        heightAnimation.run((localHeight + 5.5));
        height = (float) heightAnimation.getValue();
        dragging.setWidth((float) width);
        dragging.setHeight(height);
    }
}
