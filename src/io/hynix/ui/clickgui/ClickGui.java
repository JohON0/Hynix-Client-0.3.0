// DropDown.java

package io.hynix.ui.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.HynixMain;
import io.hynix.ui.clickgui.elements.*;
import io.hynix.ui.clickgui.elements.Panel;
import io.hynix.ui.clickgui.snow.Snow;
import io.hynix.ui.configui.ConfigUI;
import io.hynix.ui.exitmenu.ExitUI;
import io.hynix.units.api.Category;
import io.hynix.managers.theme.Theme;
import io.hynix.ui.clickgui.components.ModuleComponent;
import io.hynix.utils.johon0.animations.easing.CompactAnimation;
import io.hynix.utils.johon0.animations.easing.Easing;
import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.client.IMinecraft;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.johon0.math.Vector2i;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.johon0.render.other.GLUtils;
import io.hynix.utils.johon0.render.other.KawaseBlur;
import io.hynix.utils.text.font.ClientFonts;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Getter
public class ClickGui extends Screen implements IMinecraft {
    private static final CompactAnimation yGuiAnimation = new CompactAnimation(Easing.EASE_OUT_EXPO, 650L);
    private static final CompactAnimation xGuiAnimation = new CompactAnimation(Easing.EASE_OUT_EXPO, 650L);
    public static boolean themelightofdark = false;
    public static float themeswapper = 0;
    @Getter
    public static final Animation globalAnim = new Animation();
    @Getter
    private static final Animation imageAnimation = new Animation();
    @Getter
    public static final Animation gradientAnimation = new Animation();
    private static final CompactAnimation scaleAnimation = new CompactAnimation(Easing.EASE_IN_QUAD, 200);
    public static float scale = 1F;


    private final List<Panel> panels = new ArrayList<>();
    @Setter
    @Getter
    private ModuleComponent expandedModule = null;
    private float updownPanel = 40;
    private float movePanel = 0;
    public Snow snow;
    public static SearchField searchField;
    public WaterMark waterMark;
    private ThemeButton themeButton;
    private ConfigButton configButton;
    public static boolean exit = false, open = false;
    public static String descToRender = "";
    public static boolean ruleBind;

    // Цвета для Худа
    public static int backgroundColor;
    public static int textcolor;
    public static int lightcolor;


    //Цвета для GUI
    public static int backgroundpanelcolor;
    public static int lighttextcolor;
    public static int lightcolorgui;
    public static int modescolor;


    private final TimerUtils psChatYAnimTimer = new TimerUtils();
    private final TimerUtils psChatOverlayAnimTimer = new TimerUtils();

    //анимация описания
    public static float animdesc;

    public ClickGui(ITextComponent titleIn) {
        super(titleIn);
        Category[] categories = Category.values();
        for (Category category : categories) {
            panels.add(new Panel(category));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        globalAnim.animate(1, 2f, Easings.EXPO_OUT);
        gradientAnimation.animate(1, 0.7f, Easings.EXPO_OUT);
        imageAnimation.animate(1, 0.5, Easings.BACK_OUT);

        exit = false;
        open = true;
        snow = new Snow(ClientUtils.calc(mc.getMainWindow().getScaledWidth()), ClientUtils.calc(mc.getMainWindow().getScaledHeight()));

        waterMark = new WaterMark(ClientUtils.calc((int) (mc.getMainWindow().getScaledWidth() / 2f))-40, ClientUtils.calc(mc.getMainWindow().getScaledHeight()) - 19, 85, 16, ClientUtils.getUsername());
        themeButton = new ThemeButton((int) (ClientUtils.calc(mc.getMainWindow().getScaledWidth()) - 5 - (ClientFonts.icons_wex[25].getWidth("B") + 10 + ClientFonts.tenacityBold[15].getWidth(themelightofdark ? "Светлая" : "Темная"))), ClientUtils.calc(mc.getMainWindow().getScaledHeight()) - 19, (int) (ClientFonts.icons_wex[25].getWidth("B") + 5 + ClientFonts.tenacityBold[15].getWidth(themelightofdark ? "Светлая" : "Темная")) + 3, 16, ClientUtils.getUsername());
        searchField = new SearchField(3, ClientUtils.calc(mc.getMainWindow().getScaledHeight()) - 19, 70, 16, "Поиск");
        configButton = new ConfigButton((int) (ClientUtils.calc(mc.getMainWindow().getScaledWidth()) - 5 - (ClientFonts.icons_wex[25].getWidth("B") + 10 + ClientFonts.tenacityBold[15].getWidth("Скачать конфиги"))), ClientUtils.calc(mc.getMainWindow().getScaledHeight()) - 42, (int) (ClientFonts.icons_wex[25].getWidth("B") + 5 + ClientFonts.tenacityBold[15].getWidth("Скачать конфиги")) + 3, 16, ClientUtils.getUsername());

        super.init();
    }

    @Override
    public void closeScreen() {
        super.closeScreen();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {

        if (ClientUtils.ctrlIsDown()) {
            movePanel += (float) (delta * 5);
        } else {
            updownPanel -= (float) (delta * 20);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        themeswapper = MathUtils.fast(themeswapper, themelightofdark ? 0 : 1, 5);

        backgroundColor = ColorUtils.interpolateColors(ColorUtils.rgba(5, 5, 5, 200), ColorUtils.rgba(255, 255, 255, 200), themeswapper);
        textcolor = ColorUtils.interpolateColors(-1, Color.black.getRGB(), themeswapper);
        lightcolor = ColorUtils.interpolateColors(ColorUtils.rgba(20, 20, 20, 200), ColorUtils.rgba(200, 200, 200, 200), themeswapper);

        //gui
        backgroundpanelcolor = ColorUtils.interpolateColors(ColorUtils.rgba(10, 10, 10, 255), ColorUtils.rgba(230, 230, 230, 255), themeswapper);
        lightcolorgui = ColorUtils.interpolateColors(ColorUtils.rgba(40, 40, 40, 255), ColorUtils.rgba(200, 200, 200, 255), themeswapper);
        lighttextcolor = ColorUtils.interpolateColors(ColorUtils.rgb(200, 200, 200), ColorUtils.rgb(50, 50, 50), themeswapper);
        modescolor = ColorUtils.interpolateColors(ColorUtils.rgba(35, 35, 35, 255), ColorUtils.rgba(200, 200, 200, 255), themeswapper);

        Stream.of(globalAnim, imageAnimation, gradientAnimation).forEach(Animation::update);
        scaleAnimation.run(exit ? 1.5 : 1);
        scaleAnimation.setDuration(exit ? 500 : 150);
        scaleAnimation.setEasing(exit ? Easing.EASE_IN_QUAD : Easing.EASE_OUT_QUAD);

        boolean allow = !(globalAnim.getValue() > 0.4);

        if (Stream.of(globalAnim, imageAnimation, gradientAnimation).allMatch(anim -> anim.getValue() <= 0.1 && anim.isDone())) {
            closeScreen();
        }
        float off = 10.0F;
        float width = (float) panels.size() * 115;
        updateScaleBasedOnScreenWidth();
        int windowWidth = ClientUtils.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientUtils.calc(mc.getMainWindow().getScaledHeight());
        Vector2i fixMouse = adjustMouseCoordinates(mouseX, mouseY);
        Vector2i fix = ClientUtils.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        mc.gameRenderer.setupOverlayRendering(2);

        if (io.hynix.units.impl.display.ClickGui.background.getValue()) {
            RenderUtils.drawContrast(1 - (float) (gradientAnimation.getValue() / 3f) * 0.7f);
            RenderUtils.drawWhite((float) gradientAnimation.getValue() * 0.7f);
        }

        if (io.hynix.units.impl.display.ClickGui.blur.getValue()) {
            KawaseBlur.blur.updateBlur((io.hynix.units.impl.display.ClickGui.blurPower.getValue() - 1), (io.hynix.units.impl.display.ClickGui.blurPower.getValue().intValue()));
            KawaseBlur.blur.BLURRED.draw();
        }

        if (io.hynix.units.impl.display.ClickGui.gradient.getValue()) {
            RenderUtils.drawRectHorizontalW(0, 0 - scaled().y / 4, Minecraft.getInstance().getMainWindow().getScaledWidth(), Minecraft.getInstance().getMainWindow().getScaledHeight() + scaled().y / 3, ColorUtils.setAlpha(Theme.mainRectColor, (int) ((255 * gradientAnimation.getValue()) * getGlobalAnim().getValue())), ColorUtils.rgba(0,0,0,0));
        }
        if (HynixMain.getInstance().getModuleManager().getClickGui().snow.getValue()) {
            snow.render(matrixStack);
        }
        themeButton.render(matrixStack, mouseX, mouseY, partialTicks);
        configButton.render(matrixStack,mouseX,mouseY,partialTicks);
        searchField.render(matrixStack, mouseX, mouseY, partialTicks);

        GLUtils.scaleStart(mc.getMainWindow().getScaledWidth() / 2F, mc.getMainWindow().getScaledHeight() / 2f, (float) scaleAnimation.getValue());
        for (Panel panel : panels) {
            xGuiAnimation.run(movePanel);
            yGuiAnimation.run(!allow ? (windowHeight / 2.0F - 110.0F - updownPanel) : (panel.getY() - (exit ? 0 : 10)));
            panel.setY((float) yGuiAnimation.getValue());
            panel.setX((float) (((windowWidth / 2f) - (width / 2f) + panel.getCategory().ordinal() * (115 + off / 2) - off / 1.5) - xGuiAnimation.getValue()));
            float animdesc1;
            for(ModuleComponent o : panel.getModules()) {

                if (MathUtils.isHovered(mouseX, mouseY, o.getX(), o.getY(), o.getWidth(), o.getHeight())) {
//
                    descToRender = o.getModule().getDesc();
                }
            }

            panel.render(matrixStack, (float) mouseX, (float) mouseY);

        }
        if (descToRender != null && !descToRender.isEmpty()) {
            //нужна девочка на отсос под стол, чтобы код работал
            int colordesc = ColorUtils.rgba(255, 255, 255,255);
            ClientFonts.tenacityBold[16].drawCenteredStringWithOutline(matrixStack, descToRender, ClientUtils.calc((int) (mc.getMainWindow().getScaledWidth() / 2f)),
                    ClientUtils.calc(mc.getMainWindow().getScaledHeight()) - 12, colordesc);
        }
        GLUtils.scaleEnd();
        mc.gameRenderer.setupOverlayRendering();
    }

    public boolean isSearching() {
        return !searchField.isEmpty();
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public boolean searchCheck(String text) {
        return isSearching() && !text.replaceAll(" ", "").toLowerCase().contains(getSearchText().replaceAll(" ", "").toLowerCase());
    }

    private void updateScaleBasedOnScreenWidth() {
        float totalPanelWidth = (float) panels.size() * 115;
        float screenWidth = (float) mc.getMainWindow().getScaledWidth();
        if (totalPanelWidth >= screenWidth) {
            scale = screenWidth / totalPanelWidth;
            scale = MathHelper.clamp(scale, 0.5F, 1.0F);
        } else {
            scale = 1.0F;
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        ruleBind = false;

        for (Panel panel : panels) {
            panel.keyPressed(keyCode, scanCode, modifiers);
        }

        if (searchField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        for (Panel panel : panels) {
            if (panel.binding) {
                ruleBind = true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            movePanel += (float) (5);
        }
        if ( keyCode == GLFW.GLFW_KEY_RIGHT) {
            movePanel -= (float) (5);
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !exit && !searchField.isTyping() && !ruleBind) {
            globalAnim.animate(0.0, 0.4, Easings.EXPO_OUT);
            gradientAnimation.animate(0.0, 0.35, Easings.EXPO_OUT);
            imageAnimation.animate(0.0, 0.3, Easings.BACK_OUT);

            exit = true;
            open = false;
            return false;
        } else {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) return false;
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    private Vector2i adjustMouseCoordinates(int mouseX, int mouseY) {
        int windowWidth = mc.getMainWindow().getScaledWidth();
        int windowHeight = mc.getMainWindow().getScaledHeight();
        float adjustedMouseX = ((float) mouseX - (float) windowWidth / 2.0F) / scale + (float) windowWidth / 2.0F;
        float adjustedMouseY = ((float) mouseY - (float) windowHeight / 2.0F) / scale + (float) windowHeight / 2.0F;
        return new Vector2i((int) adjustedMouseX, (int) adjustedMouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vector2i fix = ClientUtils.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        if (searchField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        for (Panel panel : panels) {
            if (panel.mouseClick((float) mouseX, (float) mouseY, button)) {
                return true;
            }
        }

        if (RenderUtils.isInRegion(mouseX,mouseY,(int) (ClientUtils.calc(mc.getMainWindow().getScaledWidth()) - 5 - (ClientFonts.icons_wex[25].getWidth("B") + 5 + ClientFonts.tenacityBold[15].getWidth(themelightofdark ? "Светлая" : "Темная"))), ClientUtils.calc(mc.getMainWindow().getScaledHeight()) - 19, (int) (ClientFonts.icons_wex[25].getWidth("B") + 5 + ClientFonts.tenacityBold[15].getWidth(themelightofdark ? "Светлая" : "Темная")) + 2, 16)) {
            themelightofdark = !themelightofdark;
        }
        if (RenderUtils.isInRegion(mouseX,mouseY,(int) (ClientUtils.calc(mc.getMainWindow().getScaledWidth()) - 5 - (ClientFonts.icons_wex[25].getWidth("B") + 10 + ClientFonts.tenacityBold[15].getWidth("Скачать конфиги"))), ClientUtils.calc(mc.getMainWindow().getScaledHeight()) - 42,
                (int) (ClientFonts.icons_wex[25].getWidth("B") + 5 + ClientFonts.tenacityBold[15].getWidth("Скачать конфиги")) + 3, 16)) {
            System.out.println("хуя скачал");
            closeScreen();
            mc.displayGuiScreen(new ConfigUI(new StringTextComponent("A")));

        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vector2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vector2i fix = ClientUtils.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = (double) fix.getX();
        mouseY = (double) fix.getY();

        for (Panel panel : panels) {
            panel.mouseRelease((float) mouseX, (float) mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchField.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

}
