package io.hynix.ui.hud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.hynix.events.impl.EventRender2D;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.ui.hud.updater.ElementRenderer;
import io.hynix.utils.johon0.render.render2d.RenderUtils;

import io.hynix.utils.player.PlayerUtils;
import io.hynix.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ChatScreen;

/**
 * @author JohON0
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ClientInfo implements ElementRenderer {

    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack ms = eventRender2D.getMatrixStack();
        float x = 4;
        float y = mc.currentScreen instanceof ChatScreen ? mc.getMainWindow().getScaledHeight() - 24 : mc.getMainWindow().getScaledHeight() - 10;
        String pos = "Position: " + (int) (mc.player.getPosX()) + ", " + (int) (mc.player.getPosY()) + ", " + (int) (mc.player.getPosZ());
        float bps = (float) PlayerUtils.getEntityBPS(mc.player, true);
        String formattedBps = "BPS: " + String.format("%.1f", bps);
        float width = ClientFonts.tenacity[12].getWidth(formattedBps + " / " + pos) + 5;
        float height = ClientFonts.tenacity[12].getFontHeight()+ 5;
        RenderUtils.drawShadow(x-2, y-5, (float) width, height, 5, ClickGui.backgroundColor);
        RenderUtils.drawRoundedRect(x-2, y-5, (float) width, height, 4, ClickGui.backgroundColor);
        ClientFonts.tenacity[12].drawString(ms,  formattedBps + " / " + pos, x, y+2, ClickGui.textcolor);
    }
}
