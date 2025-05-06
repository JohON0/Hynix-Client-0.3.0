package io.hynix.utils.johon0.render.other;

import com.mojang.blaze3d.platform.GlStateManager;
import io.hynix.utils.client.IMinecraft;
import lombok.experimental.UtilityClass;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

@UtilityClass
public class GLUtils implements IMinecraft {

    public void rescale(double width, double height) {
        GlStateManager.clear(256);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translatef(0.0F, 0.0F, -2000.0F);
    }

    public void enableDepth() {
        GlStateManager.enableDepthTest();
        GlStateManager.depthMask(true);
    }

    public void disableDepth() {
        GlStateManager.disableDepthTest();
        GlStateManager.depthMask(false);
    }

    public void startBlend() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void endBlend() {
        GlStateManager.disableBlend();
    }

    public void setup2DRendering(boolean blend) {
        if (blend) {
            startBlend();
        }
        GlStateManager.disableTexture();
    }

    public void setup2DRendering() {
        setup2DRendering(true);
    }

    public void end2DRendering() {
        GlStateManager.enableTexture();
        endBlend();
    }

    public void startRotate(float x, float y, float rotate) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef(x, y, 0);
        GlStateManager.rotatef(rotate, 0, 0, -1);
        GlStateManager.translatef(-x, -y, 0);
    }

    public void endRotate() {
        GlStateManager.popMatrix();
    }

    public void scaleStart(float x, float y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, 0);
        GlStateManager.scaled(scale, scale, 1);
        GlStateManager.translated(-x, -y, 0);
    }

    public void scaleEnd() {
        GlStateManager.popMatrix();
    }

    public void rotate(float x, float y, float rotate, Runnable runnable) {
        GlStateManager.pushMatrix();
        GlStateManager.translatef(x, y, 0);
        GlStateManager.rotatef(rotate, 0, 0, -1);
        GlStateManager.translatef(-x, -y, 0);
        runnable.run();
        GlStateManager.popMatrix();
    }

}
