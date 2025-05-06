package io.hynix.ui.hud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.hynix.events.impl.EventRender2D;
import io.hynix.managers.premium.PremiumChecker;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.ui.hud.updater.ElementRenderer;
import io.hynix.managers.theme.Theme;
import io.hynix.utils.attackdev.HudUtils;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;

import io.hynix.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector4f;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Watermark implements ElementRenderer {

    private int fpsAnim, pingAnim,fpsint;

    @Override
    public void render(EventRender2D eventRender2D) {
        fpsAnim = (int) (Minecraft.getInstance().debugFPS);
        pingAnim = (int) (MathUtils.calculatePing());
        MatrixStack ms = eventRender2D.getMatrixStack();
        float x = 4;
        float y = 4;

        String logo = "F", fps = "A", ping = "C";
        String text = "Hynix";

        // Ширина текста
        float logoWidth = ClientFonts.dev[30].getWidth(logo);
        float textWidth = ClientFonts.tenacity[16].getWidth(text);
        float fpsWidth = ClientFonts.dev[30].getWidth(fps);
        float fpsIntWidth = ClientFonts.tenacity[16].getWidth(fpsAnim + " FPS");
        float pingWidth = ClientFonts.dev[30].getWidth(ping);
        float pingIntWidth = ClientFonts.tenacity[18].getWidth(HudUtils.calculate() + " MS");

        // Вычисляем итоговую ширину с отступами
        float padding = 6.0f; // Отступы между текстами
        float widthrect = logoWidth + textWidth + fpsWidth + fpsIntWidth + pingWidth + pingIntWidth
                + 4 * padding + 20.0f; // 4 отступа между 5 элементами
        // Рисуем фон
        RenderUtils.drawShadow(x, y, widthrect, 20, 5, ClickGui.backgroundColor);
        RenderUtils.drawRoundedRect(x, y, widthrect, 20, PremiumChecker.isPremium ? new Vector4f(4,4,4,4) : new Vector4f(4,4,4,4), ClickGui.backgroundColor);
        // Рисуем текст с учетом отступов
        ClientFonts.dev[40].drawString(ms, logo, x + padding, y + 1.5f, Theme.rectColor);
        ClientFonts.tenacity[18].drawString(ms, text, x + logoWidth + padding * 2, y + 9, ClickGui.textcolor);
        ClientFonts.dev[30].drawString(ms, fps, x + logoWidth + textWidth + padding * 3.5f, y + 6, Theme.rectColor);
        ClientFonts.tenacity[18].drawString(ms, fpsAnim + " FPS", x + logoWidth + textWidth + fpsWidth + padding * 4, y + 9, ClickGui.textcolor);
        ClientFonts.dev[30].drawString(ms, ping, x + logoWidth + textWidth + fpsWidth + fpsIntWidth + padding * 5.5f, y + 6, Theme.rectColor);
        ClientFonts.tenacity[18].drawString(ms, HudUtils.calculate() + " MS", x + logoWidth + textWidth + fpsWidth + fpsIntWidth + pingWidth + padding * 6, y + 9, ClickGui.textcolor);
        //чек на премиум
        if (PremiumChecker.isPremium) {
            int widthpremiumrect = 20;
            int heightpremiumrect = 20;
            RenderUtils.drawShadow(x, y + 22, widthpremiumrect, heightpremiumrect, 5, ClickGui.backgroundColor);
            RenderUtils.drawRoundedRect(x, y + 22, widthpremiumrect, heightpremiumrect, new Vector4f(4, 4, 4, 4), ClickGui.backgroundColor);
            ClientFonts.dev[35].drawString(ms, "J", x + padding - 1, y + 25, Theme.rectColor);
        }

    }

}
