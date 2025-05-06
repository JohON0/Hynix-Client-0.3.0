package io.hynix.ui.clickgui.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.HynixMain;
import io.hynix.managers.theme.Theme;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.units.api.Unit;
import io.hynix.units.impl.miscellaneous.BetterMinecraft;
import io.hynix.units.settings.api.Setting;
import io.hynix.units.settings.impl.*;
import io.hynix.ui.clickgui.elements.Panel;
import io.hynix.ui.clickgui.components.builder.Component;
import io.hynix.ui.clickgui.components.settings.*;
import io.hynix.utils.client.SoundPlayer;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.math.Vector4i;
import io.hynix.utils.johon0.render.Cursors;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.johon0.render.other.Stencil;
import io.hynix.utils.johon0.render.render2d.RectUtils;
import io.hynix.utils.text.BetterText;
import io.hynix.utils.text.GradientUtil;
import io.hynix.utils.text.font.ClientFonts;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;
import io.hynix.utils.client.KeyStorage;

import java.util.List;

@Getter
public class ModuleComponent extends Component {
    private final Vector4f ROUNDING_VECTOR = new Vector4f(5, 5, 5, 5);
    private final Vector4i BORDER_COLOR = new Vector4i(ColorUtils.rgb(45, 46, 53), ColorUtils.rgb(25, 26, 31), ColorUtils.rgb(45, 46, 53), ColorUtils.rgb(25, 26, 31));
    private final Unit module;
    protected Panel panel;
    public Animation expandAnim = new Animation();

    public Animation hoverAnim = new Animation();

    public Animation bindAnim = new Animation();
    public Animation noBindAnim = new Animation();

    public boolean open;

    public boolean bind;

    private double openAnimValue = 0.3, noOpenAnimValue = 0.4;

    private final ObjectArrayList<Component> components = new ObjectArrayList<>();

    public ModuleComponent(Unit module) {
        this.module = module;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting bool) {
                components.add(new BooleanComponent(bool));
            }
            if (setting instanceof SliderSetting slider) {
                components.add(new SliderComponent(slider));
            }
            if (setting instanceof BindSetting bind) {
                components.add(new BindComponent(bind));
            }
            if (setting instanceof ModeSetting mode) {
                components.add(new ModeComponent(mode));
            }
            if (setting instanceof ModeListSetting mode) {
                components.add(new MultiBoxComponent(mode));
            }
            if (setting instanceof StringSetting string) {
                components.add(new StringComponent(string));
            }
            if (setting instanceof ColorSetting color) {
                components.add(new ColorComponent(color));
            }
        }
        expandAnim = expandAnim.animate(open ? 1 : 0, open ? openAnimValue : noOpenAnimValue, Easings.EXPO_OUT);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        for (Component component : components) {
            component.mouseRelease(mouseX, mouseY, mouse);
        }

        super.mouseRelease(mouseX, mouseY, mouse);
    }

    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);

        module.getAnimation().update();
        betterText.update();

        if (HynixMain.getInstance().getClickGuiScreen().getExpandedModule() != this) open = false;

        boolean hover = MathUtils.isHovered(mouseX, mouseY, getX() + 0.5f, getY() + 0.5f, getWidth() - 1, getHeight());

        if (hover) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
        }

        hoverAnim.animate(hover ? 1 : 0, 0.3, Easings.BACK_OUT);
        bindAnim.animate(bind ? 1 : 0, 0.3, Easings.BACK_OUT);
        noBindAnim.animate(!bind ? 1 : 0, 0.3, Easings.BACK_OUT);
        double posAnim = (1.5 * hoverAnim.getValue());

        int color = ColorUtils.interpolate(ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())), ColorUtils.setAlpha(ClickGui.lighttextcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())), (float) module.getAnimation().getValue());
        int rectAlpha = (int) ((20 * module.getAnimation().getValue()) * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue());
        int hoverColorWhenNoActive = ColorUtils.interpolateColor(ColorUtils.rgb(200,200,200), ColorUtils.rgba(100,100,100,255), (float) hoverAnim.getValue());
        int rectColor = module.isEnabled() ? ColorUtils.setAlpha(ColorUtils.rgba(100,100,100,255), rectAlpha) : ColorUtils.setAlpha(hoverColorWhenNoActive, (int) (15 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue()));
        int statusColor = ColorUtils.setAlpha(ColorUtils.interpolateColor(ClickGui.textcolor, ColorUtils.rgba(100,100,100,255), (float)module.getAnimation().getValue()), (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue()));
        BetterMinecraft betterMinecraft = HynixMain.getInstance().getModuleManager().getBetterMinecraft();
        float offMe = (float) (0.5f + (HynixMain.getInstance().getModuleManager().getBetterMinecraft().isEnabled() && betterMinecraft.fpsBoot.getValue() ? 1 : 2) * hoverAnim.getValue());

        RectUtils.getInstance().drawRoundedRectShadowed(stack, getX() + offMe, getY() + offMe, getX() + getWidth() - offMe, getY() + getHeight() - offMe, 3, (float) (3 * hoverAnim.getValue()), rectColor, rectColor, rectColor, rectColor, false, false, true, hoverAnim.getValue() > 0.01);

        int colorForModuleText = ColorUtils.setAlpha(color, (int) ((255 * noBindAnim.getValue() * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        int colorpremicon = ColorUtils.setAlpha(ColorUtils.rgba(255,215,0,255), (int) ((255 * noBindAnim.getValue() * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));
        int colorForBindText = ColorUtils.interpolateColor(ColorUtils.rgba(140, 140, 140, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())), ColorUtils.setAlpha(ColorUtils.rgb(140, 140, 140), 0), (float) bindAnim.getValue());

        ClientFonts.tenacityBold[17].drawCenteredString(stack, module.getName(),  (float)(getX() + 55), getY() + 6.5F, colorForModuleText);
        if (module.isPremium()) {
            ClientFonts.dev[25].drawString(stack, "J", (float) (getX() + 5), getY() + 5.5F, colorpremicon);
        }

        if (this.components.stream().filter(Component::isVisible).count() >= 1L) {
            ClientFonts.icon[14].drawString(stack, open ? "v" : "x", getX() + getWidth() - 12, getY() + 8, statusColor);
        }

        ClientFonts.tenacityBold[17].drawString(stack, "Bind" + (module.getBind() == 0 ? betterText.getOutput() : ": " + KeyStorage.getReverseKey(module.getBind())), (float)(getX() + 6 + posAnim), getY() + 6F, colorForBindText);

        if (expandAnim.getValue() > 0) {
            if (components.stream().filter(Component::isVisible).count() >= 1) {
            }
            Stencil.initStencilToWrite();
            RenderUtils.drawRoundedRect(getX() + 0.5f, getY() + 0.5f, getWidth() - 1, getHeight() - 1, ROUNDING_VECTOR, ColorUtils.rgba(23, 23, 23, (int) (255 * 0.33)));
            Stencil.readStencilBuffer(1);

            float y = getY() + 20;
            for (Component component : components) {
                if (component.isVisible()){
                    component.setX(getX());
                    component.setY(y);
                    component.setWidth(getWidth());
                    component.render(stack, mouseX, mouseY );
                    y += component.getHeight();
                }
            }
            Stencil.uninitStencilBuffer();
        }
    }

    @Override
    public boolean mouseClick(float mouseX, float mouseY, int button) {
        if (MathUtils.isHovered(mouseX, mouseY, getX() + 1, getY() + 1, getWidth() - 2, 18)) {
            ModuleComponent openModule = HynixMain.getInstance().getClickGuiScreen().getExpandedModule();
            if (openModule != null && openModule != this && button == 1 && !module.getSettings().isEmpty()) {
                openModule.open = false;
                openModule.expandAnim.animate(0, noOpenAnimValue, Easings.EXPO_OUT);
            }
            if (button == 0 && !bind) module.toggle();
            if (button == 1 && !bind && expandAnim.isDone()) {
                if (!module.getSettings().isEmpty()) {
                    open = !open;
                    SoundPlayer.playSound(open ? "moduleonopen.wav" : "moduleonclose.wav");
                    if (expandAnim.isDone()) SoundPlayer.playSound(open ? "moduleopen.wav" : "moduleclose.wav");

                    if (open) {
                        HynixMain.getInstance().getClickGuiScreen().setExpandedModule(this);
                        expandAnim = expandAnim.animate(1, openAnimValue, Easings.EXPO_OUT);
                    }

                    expandAnim = expandAnim.animate(open ? 1 : 0, open ? openAnimValue : noOpenAnimValue, Easings.EXPO_OUT);
                }
            }
            if (button == 2) {
                bind = !bind;

                SoundPlayer.playSound(bind ? "guibindingstart.wav" : "guibindingnull.wav");
            }
        }
        if (isHovered(mouseX, mouseY)) {
            if (open) {
                for (Component component : components) {
                    if (component.isVisible()) component.mouseClick(mouseX, mouseY, button);
                }
            }
        }
        super.mouseClick(mouseX, mouseY, button);
        return false;
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.charTyped(codePoint, modifiers);
        }
        super.charTyped(codePoint, modifiers);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.keyPressed(key, scanCode, modifiers);
        }
        if (bind) {
            if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_ESCAPE) {
                module.setBind(0);
                SoundPlayer.playSound("guibindreset.wav");
            } else {
                module.setBind(key);
                SoundPlayer.playSound("guibinding.wav");
            }
            bind = false;
        }
        super.keyPressed(key, scanCode, modifiers);
    }

    private final BetterText betterText = new BetterText(List.of(
            "...", "...", "..."
    ), 100);
}