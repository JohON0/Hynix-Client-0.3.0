package io.hynix.ui.clickgui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.HynixMain;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.ui.clickgui.components.builder.IBuilder;
import io.hynix.ui.clickgui.components.builder.Component;
import io.hynix.ui.clickgui.components.ModuleComponent;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Panel implements IBuilder {

    private final Category category;
    protected float x;
    protected float y;
    protected final float width = 115;
    protected float height;

    public boolean binding;

    private List<ModuleComponent> modules = new ArrayList<>();
    private float scroll, animatedScrool;

    public Panel(Category category) {
        this.category = category;

        for (Unit module : HynixMain.getInstance().getModuleManager().getModules()) {
            if (module.getCategory() == category) {
                ModuleComponent component = new ModuleComponent(module);
                component.setPanel(this);
                modules.add(component);
            }
        }

        updateHeight();
    }

    double base = 20;
    double biba = 28.5;
    double boba = 8.5;

    public void updateHeight() {
        final double additionalHeight = modules.stream().filter(ModuleComponent::isOpen).mapToDouble(ModuleComponent::getHeight).sum();

        this.height = (float) Math.max(biba, base + additionalHeight + boba);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        animatedScrool = MathUtils.fast(animatedScrool, scroll, 10);
        float headerFont = 9;

        updateHeight();
        height = (float) Math.max(biba, modules.stream().filter(component -> !HynixMain.getInstance().getClickGuiScreen().searchCheck(component.getModule().getName())).mapToDouble(ModuleComponent::getHeight).sum() + base + boba);

        RenderUtils.drawShadowFancyRectNoOutline(stack, x, y, width, height,(int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue()));

        ClientFonts.tenacityBold[20].drawCenteredString(stack, category.name(), x + 57, y + 13 - ClientFonts.tenacityBold[18].getFontHeight() / 2f, ColorUtils.setAlpha(ClickGui.textcolor, (int) (255 * io.hynix.ui.clickgui.ClickGui.getGlobalAnim().getValue())));

        drawComponents(stack, mouseX, mouseY);
    }

    float max = 0;

    private void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        float offset = -1;
        float header = 25;

        if (max > height - header - 10) {
            scroll = MathHelper.clamp(scroll, -max + height - header - 10, 0);
            animatedScrool = MathHelper.clamp(animatedScrool, -max + height - header - 10, 0);
        } else {
            scroll = 0;
            animatedScrool = 0;
        }

        for (ModuleComponent component : modules) {
            component.setX(getX() + 0.5f);
            component.setY(getY() + header + offset + 0.5f + animatedScrool);
            component.setWidth(getWidth() - 1);
            component.setHeight(20);
            component.expandAnim.update();
            component.hoverAnim.update();
            component.bindAnim.update();
            component.noBindAnim.update();
            binding = component.bind;

            if (HynixMain.getInstance().getClickGuiScreen().searchCheck(component.getModule().getName())){
                continue;
            }

            if (component.expandAnim.getValue() > 0 && HynixMain.getInstance().getClickGuiScreen().getExpandedModule() == component) {
                float componentOffset = 0;
                for (Component component2 : component.getComponents()) {
                    if (component2.isVisible())
                        componentOffset += component2.getHeight();
                }
                componentOffset *= (float) component.expandAnim.getValue();
                component.setHeight(component.getHeight() + componentOffset);
            }

            component.render(stack, mouseX, mouseY);
            offset += component.getHeight() + 0.1f;
        }

        max = offset;
    }

    @Override
    public boolean mouseClick(float mouseX, float mouseY, int button) {
        for (ModuleComponent component : modules) {
            if (HynixMain.getInstance().getClickGuiScreen().searchCheck(component.getModule().getName())){
                continue;
            }
            component.mouseClick(mouseX, mouseY, button);
        }
        return false;
    }


    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (ModuleComponent component : modules) {
            component.keyPressed(key, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (ModuleComponent component : modules) {
            component.charTyped(codePoint, modifiers);
        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int button) {
        for (ModuleComponent component : modules) {
            component.mouseRelease(mouseX, mouseY, button);
        }
    }

}