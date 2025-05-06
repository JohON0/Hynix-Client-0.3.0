package io.hynix.ui.exitmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.impl.EaseBackIn;
import io.hynix.utils.johon0.animations.impl.EaseInOutQuad;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static io.hynix.utils.client.IMinecraft.mc;


public class ExitUI extends Screen {
    public ExitUI(ITextComponent titleIn) {
        super(titleIn);
    }

    float timing = 0;
    float anim = 0;
    public final TimerUtils timer = new TimerUtils();
    public static EaseInOutQuad animate = new EaseInOutQuad(500,1);

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        animate.setDirection(Direction.FORWARDS);
        animate.reset();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        mc.gameRenderer.setupOverlayRendering(2);
        RenderUtils.drawShader(timer);
        anim = MathUtils.fast(anim, timing > 100 ? 0 : 1,2);
        GL11.glPushMatrix();
        GL11.glTranslatef(mc.getMainWindow().getScaledWidth()/2f,mc.getMainWindow().getScaledHeight()/2f,0);
        GL11.glScalef((float) animate.getOutput(), (float) animate.getOutput(),0);
        GL11.glTranslatef(-mc.getMainWindow().getScaledWidth()/2f,-mc.getMainWindow().getScaledHeight()/2f,0);
        ClientFonts.tenacityBold[20].drawCenteredString(matrixStack,"До свидания!",mc.getMainWindow().getScaledWidth()/2f,mc.getMainWindow().getScaledHeight()/2f, ColorUtils.reAlphaInt(-1, (int) (255 * anim)));
        ClientFonts.tenacityBold[20].drawCenteredString(matrixStack,"Надеюсь ещё зайдешь! =)",mc.getMainWindow().getScaledWidth()/2f,mc.getMainWindow().getScaledHeight()/2f+10, ColorUtils.reAlphaInt(-1, (int) (255 * anim)));

        GL11.glPopMatrix();

        if( timing > 200){
            Minecraft.getInstance().shutdownMinecraftApplet();
        }
        timing++;
        mc.gameRenderer.setupOverlayRendering();
    }

    @Override
    public void onClose() {
        super.onClose();
        timing = 0;
    }
}
