package io.hynix.ui.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.HynixMain;
import io.hynix.managers.config.AltConfig;
import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.client.IMinecraft;
import io.hynix.utils.johon0.math.Vector2i;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.math.TimerUtils;

import io.hynix.utils.player.MouseUtils;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RectUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.johon0.render.other.Scissor;
import io.hynix.utils.text.font.ClientFonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Session;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AltScreen extends Screen implements IMinecraft {

    public AltScreen() {
        super(new StringTextComponent(""));
    }

    public final TimerUtils timer = new TimerUtils();

    public final List<Alt> alts = new ArrayList<>();

    public float scroll;
    public float scrollAn;

    private String altName = "";
    private boolean typing;
    float minus = 14;
    float offset = 6f;
    float width = 290, height = 210;

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        scrollAn = MathUtils.lerp(scrollAn, scroll, 5);

        RenderUtils.drawShader(timer);

        mc.gameRenderer.setupOverlayRendering(2);

        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;


        // Квадрат фона
        float bgX = x - offset, bgY = y - offset, bgWidth = width + offset * 2, bgHeight = height + offset * 2;
        int bgRectColor = ColorUtils.rgba(5, 5, 5, 160);
        RectUtils.getInstance().drawRoundedRectShadowed(matrixStack, bgX, bgY, bgX + bgWidth, bgY + bgHeight + 35, 4, 2, bgRectColor, bgRectColor, bgRectColor, bgRectColor, false, false, true, true);

        // alt screen name
        RenderUtils.drawShadow(x - 2 + width / 2 - ClientFonts.tenacity[22].getWidth("Alt Manager") / 2, y + offset * 2 - 2, ClientFonts.tenacity[22].getWidth("Alt Manager") + 2, ClientFonts.msSemiBold[22].getFontHeight(), 10, ColorUtils.rgba(255, 255, 255, 40));
        ClientFonts.tenacity[22].drawCenteredString(matrixStack, "Alt Manager", x + width / 2, y + offset * 2 + 3, -1);
        ClientFonts.tenacityBold[16].drawCenteredString(matrixStack, "Текущий ник: " + mc.session.getUsername(), x + width / 2, y + height - offset * 2 + 35, -1);

        // Квадратик для ввода ника
        RenderUtils.drawShadow(x + offset - 1, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width + 2 - offset * 2f, 20f, 8, ColorUtils.rgba(10, 10, 10, 100));
        RenderUtils.drawRoundedRect(x + offset - 1, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width + 2 - offset * 2f, 20f, 2f, ColorUtils.rgba(10, 10, 10, 100));

        Scissor.push();
        Scissor.setFromComponentCoordinates(x + offset, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width - offset * 2f, 20f);
        ClientFonts.tenacityBold[15].drawString(matrixStack, typing ? (altName + (typing ? "_" : "")) : "Укажите свой ник!", x + offset + 5f,
                y + offset + 72 - minus * 2.5f + 177 - offset * 2, -1);
        Scissor.unset();
        Scissor.pop();

        // Знак для ввода рандомного ника
        int col = ColorUtils.rgb(38, 33, 54);
        RenderUtils.drawRoundedRect(x + width - offset - ClientFonts.tenacityBold[22].getWidth("Рандом") - offset * 2, y + offset + 64 - minus * 2.5f + 202 - offset * 2, ClientFonts.tenacityBold[22].getWidth("Random") + 13f, 20, new Vector4f(3, 3, 3, 3), ColorUtils.rgba(15, 15, 15, 70));
        ClientFonts.tenacityBold[22].drawCenteredString(matrixStack, "Рандом", x + width - offset * 2 - 20, y + offset + 64 - minus * 2.5f + 200 - offset * 2 + ClientFonts.tenacityBold[22].getFontHeight() / 2, -1);

        // Вывод никнеймов
        float dick = 1;
        RenderUtils.drawShadow(x + offset - dick, y + offset + 60f - minus * 2, width - offset * 2f + dick * 2, 177.5f - minus * 2, 8, ColorUtils.rgba(10, 10, 10, 90));
        RenderUtils.drawRoundedRect(x + offset - dick, y + offset + 60f - minus * 2, width - offset * 2f + dick * 2, 177.5f - minus * 2, 2f, ColorUtils.rgba(10, 10, 10, 90));

        float size = 0f, iter = scrollAn, offsetAccounts = 0f;

        Scissor.push();
        Scissor.setFromComponentCoordinates(x + offset, y + offset + 60f - minus * 2, width - offset * 2f, 177.5f - minus * 2);
        for (Alt alt : alts) {
            float scrollY = y + iter * 22f;
            int color = (mc.session.getUsername().equals(alt.name)) ? ColorUtils.rgba(25, 25, 25, 80) : ColorUtils.rgba(15, 15, 15, 80);

            RenderUtils.drawShadow(x + offset + 2f, scrollY + offset + 62 + offsetAccounts - minus * 2, width - offset * 2f - 4f, 20f, 6, color);
            RenderUtils.drawRoundedRect(x + offset + 2f, scrollY + offset + 62 + offsetAccounts - minus * 2, width - offset * 2f - 4f, 20f, 2f, color);

            ClientFonts.tenacityBold[18].drawCenteredString(matrixStack, alt.name, x + offset + width / 2 - 8, scrollY + offset + 68 + offsetAccounts - minus * 2, -1);

            mc.getTextureManager().bindTexture(alt.skin);

            AbstractGui.drawScaledCustomSizeModalRect(x + offset + 4f + 0.5f, scrollY + offset + 63.5f + offsetAccounts - minus * 2, 8F, 8F, 8F, 8F, 16, 16, 64, 64);

            iter++;
            size++;
        }
        scroll = MathHelper.clamp(scroll, size > 8 ? -size + 4 : 0, 0);
        Scissor.unset();
        Scissor.pop();

        mc.gameRenderer.setupOverlayRendering();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!altName.isEmpty() && typing)
                altName = altName.substring(0, altName.length() - 1);
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            if (!altName.isEmpty() && altName.length() >= 3) {
                alts.add(new Alt(altName));
                AltConfig.updateFile();
//                SoundPlayer.playSound("success.wav");
            }
            typing = false;
            altName = "";
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (typing) {
                typing = false;
                altName = "";
            }
        }

        boolean ctrlDown = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        if (typing) {
            if (ClientUtils.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_V) {
                try {
                    altName += ClientUtils.pasteString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (ClientUtils.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                try {
                    altName = "";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (altName.length() <= 20) altName += Character.toString(codePoint);
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector2i fixed = MathUtils.getMouse2i((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;

        if (button == 0 && RenderUtils.isInRegion(mouseX, mouseY, x + width - offset - ClientFonts.tenacity[22].getWidth("Рандом") - offset * 2, y + offset + 64 - minus * 2.5f + 202 - offset * 2, ClientFonts.tenacity[22].getWidth("Рандом") + 13f, 20)) {
            alts.add(new Alt(HynixMain.getInstance().randomNickname()));
            AltConfig.updateFile();
//            SoundPlayer.playSound("success.wav", 0.1f);
        }
        if (button == 0 && RenderUtils.isInRegion(mouseX, mouseY, x + offset - 1, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width + 2 - offset * 2f, 20f)
                && !RenderUtils.isInRegion(mouseX, mouseY, x + width - offset - ClientFonts.tenacity[22].getWidth("Рандом") - offset * 2, y + offset + 64 - minus * 2.5f + 202 - offset * 2, ClientFonts.tenacity[22].getWidth("Рандом") + 12f, 20)) {
            typing = !typing;
        }

        // Основной функционал позволяющий позволяющий брать/удалять ник
        float iter = scrollAn, offsetAccounts = 0f;
        Iterator<Alt> iterator = alts.iterator();
        while (iterator.hasNext()) {
            Alt account = iterator.next();

            float scrollY = y + iter * 22f;

            if (RenderUtils.isInRegion(mouseX, mouseY, x + offset + 2f, scrollY + offset + 62 + offsetAccounts - minus * 2, width - offset * 2f - 4f, 20f)) {
                if (button == 0) {
//                    SoundPlayer.playSound("altselect.wav", 0.05f);
                    mc.session = new Session(account.name, "", "", "mojang");
                } else if (button == 1) {
                    iterator.remove();
                    AltConfig.updateFile();
//                    SoundPlayer.playSound("friendremove.wav");
                }
            }

            iter++;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Vector2i fixed = MathUtils.getMouse2i((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;

        if (MouseUtils.isHovered(mouseX, mouseY, x + offset, y + offset + 60f - minus * 2, width - offset * 2f, 177.5f - minus * 2)) scroll += delta * 1;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
    }

    @Override
    public void tick() {
        super.tick();
    }
}
