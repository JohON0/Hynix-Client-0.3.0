package io.hynix.ui.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.units.settings.impl.BindSetting;
import io.hynix.ui.clickgui.components.builder.Component;
import io.hynix.utils.client.SoundPlayer;
import io.hynix.utils.client.KeyStorage;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.johon0.render.font.Fonts;
import io.hynix.utils.text.font.ClientFonts;
import org.lwjgl.glfw.GLFW;

public class BindComponent extends Component {

    final BindSetting setting;

    public BindComponent(BindSetting setting) {
        this.setting = setting;
        this.setHeight(16);
    }

    boolean activated;
    boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        ClientFonts.tenacityBold[14].drawString(stack, setting.getName(), getX() + 5, getY() + 5.5f / 2f + 1, ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        String bind = KeyStorage.getKey(setting.getValue()).toLowerCase();

        if (bind == null || setting.getValue() == -1) {
            bind = "none";
        }
        boolean next = ClientFonts.tenacityBold[14].getWidth(bind) >= 1;
        float x = next ? getX() + 5 : getX() + getWidth() - 7 - ClientFonts.tenacityBold[14].getWidth(bind);
        float y = getY() + 4f / 2f + (4f / 2f) + (next ? 8 : 0);
        RenderUtils.drawRoundedRect(x - 2, y - 2, ClientFonts.tenacityBold[14].getWidth(bind) + 4, 5.5f + 4, 2, ColorUtils.setAlpha(ClickGui.lightcolorgui, (int) ( 255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        ClientFonts.tenacityBold[14].drawString(stack, bind, x, y+1, activated ? ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())) : ColorUtils.setAlpha(ClickGui.lighttextcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));

        if (isHovered(mouseX, mouseY)) {
            if (MathUtils.isHovered(mouseX, mouseY, x - 2, y - 2, ClientFonts.tenacityBold[14].getWidth(bind) + 4, 5.5f + 4)) {
                if (!hovered) {
                    hovered = true;
                }
            } else {
                if (hovered) {
                    hovered = false;
                }
            }
        }
        setHeight(next ? 23 : 16);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        if (activated) {
            if (key == GLFW.GLFW_KEY_DELETE) {
                setting.setValue(-1);
                activated = false;
                return;
            }
            setting.setValue(key);
            activated = false;
        }
        super.keyPressed(key, scanCode, modifiers);
    }


    @Override
    public boolean mouseClick(float mouseX, float mouseY, int mouse) {
        if (isHovered(mouseX, mouseY) && mouse == 0) {
            activated = !activated;

        }

        if (activated && mouse >= 1) {
            System.out.println(-100 + mouse);
            setting.setValue(-100 + mouse);
            activated = false;
        }

        super.mouseClick(mouseX, mouseY, mouse);
        return false;
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}
