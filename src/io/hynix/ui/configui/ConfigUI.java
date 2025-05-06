package io.hynix.ui.configui;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.ui.configui.listusers.UserConfigList;
import io.hynix.ui.configui.listusers.UsersRender;
import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.client.IMinecraft;
import io.hynix.utils.johon0.render.render2d.RectUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.util.Easings;

import static io.hynix.ui.clickgui.ClickGui.*;

public class ConfigUI extends Screen implements IMinecraft {
    private UsersRender usersRender;
    private UserConfigList userConfigList;
    private io.hynix.ui.clickgui.ClickGui clickGuiScreen;

    public ConfigUI(ITextComponent titleIn) {
        super(titleIn);
    }

    @Override
    protected void init() {
        super.init();
        if (UserConfigList.getConfigs().isEmpty()) {
            UserConfigList.add("Конфиги от johon0", "https://hynix.fun/johon0cfg.zip");
            UserConfigList.add("Конфиги от Конфетки", "https://hynix.fun/konfetkicfg.zip");
            UserConfigList.add("Конфиги от attack.dev", "https://hynix.fun/attackdevcfg.zip");
            UserConfigList.add("Конфиги от Джокера", "https://hynix.fun/attackdevcfg.zip");
        }
        usersRender = new UsersRender(userConfigList);
    }
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        float widthButton = 200;
        float heightButton = 20;
        float x = ClientUtils.calc(width) / 2f - widthButton / 2;
        float y = Math.round(ClientUtils.calc(height) / 2f - (heightButton * 8) / 2);

        float widthRect = widthButton * 1.5f;
        float xRect = x - widthButton / 4;
        float heightRect = (heightButton * 8);

        int bgRectColor = ClickGui.backgroundColor;

        RectUtils.getInstance().drawRoundedRectShadowed(matrixStack, xRect, y - 25, xRect + widthRect, y - 5 + heightRect + 20, 8, 5, bgRectColor, bgRectColor, bgRectColor, bgRectColor, false, false, true, true);
        usersRender.renderUserConfigs(matrixStack, xRect + 5, y - 40, mouseX, mouseY);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !exit && !searchField.isTyping() && !ruleBind) {
            globalAnim.animate(0.0, 0.4, Easings.EXPO_OUT);
            gradientAnimation.animate(0.0, 0.35, Easings.EXPO_OUT);

            exit = true;
            open = false;
            return false;
        } else {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) return false;
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void closeScreen() {
        ClickGui.globalAnim.animate(0.0, 0.4, Easings.EXPO_OUT);
        ClickGui.gradientAnimation.animate(0.0, 0.35, Easings.EXPO_OUT);
        ClickGui.getImageAnimation().animate(0.0, 0.3, Easings.BACK_OUT);

        exit = true;
        ClickGui.open = false;
        super.closeScreen();
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
