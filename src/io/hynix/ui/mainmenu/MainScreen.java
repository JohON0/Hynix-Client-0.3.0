package io.hynix.ui.mainmenu;

import io.hynix.HynixMain;
import io.hynix.managers.id.IDManager;
import io.hynix.ui.configui.ConfigUI;
import io.hynix.ui.exitmenu.ExitUI;
import io.hynix.ui.mainmenu.changelog.Changelog;
import io.hynix.ui.mainmenu.changelog.ChangelogRender;
import io.hynix.utils.client.SoundPlayer;
import io.hynix.utils.johon0.math.animation.Animation;
import io.hynix.utils.johon0.math.animation.util.Easings;
import io.hynix.utils.johon0.render.render2d.RectUtils;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.client.IMinecraft;
import io.hynix.utils.johon0.math.Vector2i;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import lombok.Getter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class MainScreen extends Screen implements IMinecraft {
    public MainScreen() {
        super(ITextComponent.getTextComponentOrEmpty(""));
        changelog = new Changelog();
        Changelog.addChange("Чуток переделал киллауру", Changelog.ChangeType.FIX);
        Changelog.addChange("FreeCamera работает на FunTime", Changelog.ChangeType.FIX);





        changelogRenderer = new ChangelogRender(changelog);

    }
    public final TimerUtils timer = new TimerUtils();
    public static float o = 0;
    private Changelog changelog;
    private ChangelogRender changelogRenderer;
    private final List<Button> buttons = new ArrayList<>();


    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        float offset = 4;

        float widthButton = 85;
        float heightButton = 15;
        float x = ClientUtils.calc(width) / 2f - widthButton / 2;
        float y = Math.round(5 + ClientUtils.calc(height) / 2f - (heightButton * 9) / 2);
        buttons.clear();
        buttons.add(new Button(x, y, widthButton, heightButton, "SinglePlayer", () -> {
            mc.displayGuiScreen(new WorldSelectionScreen(this));
        }));
        y += heightButton + offset;
        buttons.add(new Button(x, y, widthButton, heightButton, "MultiPlayer", () -> {
            mc.displayGuiScreen(new MultiplayerScreen(this));
        }));
        y += heightButton + offset;
        buttons.add(new Button(x, y, widthButton, heightButton, "Alt Manager", () -> {
            mc.displayGuiScreen(HynixMain.getInstance().getAltScreen());
        }));
        y += heightButton + offset;
        buttons.add(new Button(x, y, widthButton, heightButton, "Options", () -> {
            mc.displayGuiScreen(new OptionsScreen(this, mc.gameSettings));
        }));
        y += heightButton + offset;
        buttons.add(new Button(x, y, widthButton, heightButton, "Configs", () -> {
            mc.displayGuiScreen(new ConfigUI(new StringTextComponent("A")));
        }));
        y += heightButton + offset;
        buttons.add(new Button(x, y, widthButton, heightButton, "Quit", () -> {
            mc.displayGuiScreen(new ExitUI(new StringTextComponent("A")));
        }));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        mc.gameRenderer.setupOverlayRendering(2);

        MainWindow mainWindow = mc.getMainWindow();

        RenderUtils.drawShader(timer);

        float widthButton = 85;
        float heightButton = 15;
        float x = ClientUtils.calc(width) / 2f - widthButton / 2;
        float y = Math.round(ClientUtils.calc(height) / 2f - (heightButton * 8) / 2);

        float widthRect = widthButton * 1.5f;
        float xRect = x - widthButton / 4;
        float heightRect = (heightButton * 7);

        int bgRectColor = ColorUtils.rgba(5,5,5, 180);

        RectUtils.getInstance().drawRoundedRectShadowed(matrixStack, xRect, y - 25, xRect + widthRect, y - 5 + heightRect + 30, 8, 5, bgRectColor, bgRectColor, bgRectColor, bgRectColor, false, false, true, true);

        RenderUtils.drawShadow(mainWindow.getScaledWidth() / 2 - 4 - (ClientFonts.tenacityBold[22].getWidth("Hynix") / 2), y - 18, ClientFonts.tenacityBold[22].getWidth("Hynix") + 8, ClientFonts.comfortaa[22].getFontHeight(), 12, ColorUtils.rgba(255, 255, 255, 40));

        ClientFonts.tenacityBold[22].drawCenteredString(matrixStack, "Hynix", mainWindow.getScaledWidth() / 2, y-18, -1);
        ClientFonts.tenacityBold[14].drawCenteredString(matrixStack, setMessage(), mainWindow.getScaledWidth() / 2 + 14.5f - (ClientFonts.tenacityBold[22].getWidth("Hynix") / 2), y + heightRect + 10, -1);
        changelog(matrixStack,mouseX,mouseY,5,5);

        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = currentTime.format(formatter);
        ClientFonts.tenacityBold[16].drawString(matrixStack, formattedTime, mainWindow.getScaledWidth() -40, 5, -1);
        ClientFonts.tenacityBold[16].drawString(matrixStack, "Ваш ID: " + IDManager.iduser, 2, mainWindow.getScaledHeight()-10, -1);

        Vector2i fixed = ClientUtils.getMouse(mouseX, mouseY);

        drawButtons(matrixStack, fixed.getX(), fixed.getY(), partialTicks);

        mc.gameRenderer.setupOverlayRendering();
    }

    private String setMessage() {
        String userName = mc.getSession().getUsername();

        return "Welcome" + ", " + userName + "!";
    }

    private void changelog(MatrixStack matrixStack,int mouseX, int mouseY,float x, float y) {
        int bgRectColor = ColorUtils.rgba(5, 5, 5, 200);

        RectUtils.getInstance().drawRoundedRectShadowed(matrixStack, x, y, 300,195,6,5, bgRectColor,bgRectColor,bgRectColor,bgRectColor, false, false, true, true);
        ClientFonts.tenacity[18].drawString(matrixStack,"Changelog",x + 5,y + 8, -1);
        changelogRenderer.renderChangelog(matrixStack, x, y);


    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector2i fixed = ClientUtils.getMouse((int) mouseX, (int) mouseY);
        buttons.forEach(b -> b.click(fixed.getX(), fixed.getY(), button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawButtons(MatrixStack stack, int mX, int mY, float pt) {
        buttons.forEach(b -> b.render(stack, mX, mY, pt));
    }

    private class Button {
        @Getter
        private final float x, y, width, height;
        private String text;
        private Runnable action;
        public Animation animation = new Animation();
        boolean hovered;

        public Button(float x, float y, float width, float height, String text, Runnable action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.action = action;
        }

        public void render(MatrixStack stack, int mouseX, int mouseY, float pt) {
            animation.update();
            animation.run(hovered ? 1 : 0, hovered ? 0.25 : 0.5, Easings.BACK_OUT);
            hovered = MathUtils.isHovered(mouseX, mouseY, x, y, width, height);
            float offset = 1.5f;
            float hoverSize = (float) (offset * animation.getValue());
            float textY = (float) (y + 5 + height / 2 - ClientFonts.tenacityBold[(int) (19 + 3 * animation.getValue())].getFontHeight() / 2);
            int interColor = ColorUtils.interpolateColor(-1, ColorUtils.rgb(200, 200, 200), (float) animation.getValue());
            int bgRectColor = ColorUtils.rgba(10, 10, 10, 200);

            RectUtils.getInstance().drawRoundedRectShadowed(stack, x, y, x + width, y + height, 6, 2, bgRectColor, bgRectColor, bgRectColor, bgRectColor, false, false, true, true);
            ClientFonts.tenacityBold[(int) (14 + 3 * animation.getValue())].drawCenteredString(stack, text, x + width / 2f, textY, ColorUtils.setAlpha(interColor, (int) (255)));
        }

        public void click(int mouseX, int mouseY, int button) {
            if (MathUtils.isHovered(mouseX, mouseY, x, y, width, height)) {
                action.run();
            }
        }

    }

}
