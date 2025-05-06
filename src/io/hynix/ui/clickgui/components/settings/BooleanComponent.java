package io.hynix.ui.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.utils.client.SoundPlayer;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.render.Cursors;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import io.hynix.ui.clickgui.components.builder.Component;

/**
 * BooleanComponent
 */
public class BooleanComponent extends Component {

    private final BooleanSetting setting;

    public BooleanComponent(BooleanSetting setting) {
        this.setting = setting;
        setHeight(16);
        animation = animation.animate(setting.getValue() ? 1 : 0, 0.1, Easings.CIRC_OUT);
    }

    private Animation animation = new Animation();
    private float width, height;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        // TODO Auto-generated method stub
        super.render(stack, mouseX, mouseY);
        animation.update();
        ClientFonts.tenacityBold[14].drawString(stack, setting.getName(), getX() + 5, getY() + 4, ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        width = 15;
        height = 7;
        int color = ColorUtils.interpolate(ColorUtils.rgb(120,120,120), -1, 1 - (float) animation.getValue());
        RenderUtils.drawRoundedRect(getX()+ getWidth() - 18, getY() - 1.5f + getHeight() / 2f - height / 2f, width, height, 3f, ColorUtils.setAlpha(ColorUtils.rgb(50,50,50), (int) (100 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        RenderUtils.drawCircle((float) (getX()+ getWidth() - 18 + 4 + (7 * animation.getValue())), getY() - 1.5f + getHeight() / 2f - height / 2f + 3.5f, 5f, ColorUtils.setAlpha(color, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));

        if (isHovered(mouseX, mouseY)) {
            if (MathUtils.isHovered(mouseX, mouseY, getX() + getWidth() + 6 , getY() - 1.5f + getHeight() / 2f - height / 2f, width, height)) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
            } else {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            }
        }
    }

    @Override
    public boolean mouseClick(float mouseX, float mouseY, int mouse) {
        if (mouse == 0 && MathUtils.isHovered(mouseX, mouseY, getX()+ getWidth() - 18, getY() - 1.5f + getHeight() / 2f - height / 2f, width, height)) {
            setting.setValue(!setting.getValue());
            animation = animation.animate(setting.getValue() ? 1 : 0, 0.1, Easings.CIRC_OUT);
            SoundPlayer.playSound(setting.getValue() ? "buttonyes.wav" : "buttonno.wav");
        }
        super.mouseClick(mouseX, mouseY, mouse);
        return false;
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}