package io.hynix.utils.johon0.render.other;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.IRenderCall;
import io.hynix.managers.theme.Theme;
import io.hynix.utils.client.IMinecraft;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.shader.ShaderUtils;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL30;

import java.util.concurrent.ConcurrentLinkedQueue;

public class OutlineShader implements IMinecraft {

    private static final ShaderUtils outline = new ShaderUtils("outline");
    private static final ConcurrentLinkedQueue<IRenderCall> renderQueue = Queues.newConcurrentLinkedQueue();
    private static final Framebuffer inFrameBuffer = new Framebuffer(1, 1, true, false);
    private static final Framebuffer outFrameBuffer = new Framebuffer(1, 1, true, false);

    public static void addTask2D(IRenderCall rc) {
        renderQueue.add(rc);
    }

    public static void draw(int radius) {
        if (renderQueue.isEmpty())
            return;

        setupBuffer(inFrameBuffer);
        setupBuffer(outFrameBuffer);

        inFrameBuffer.bindFramebuffer(true);

        while (!renderQueue.isEmpty()) {
            renderQueue.poll().execute();
        }

        float[] colors = ColorUtils.rgba(Theme.rectColor);

        outFrameBuffer.bindFramebuffer(true);

        outline.attach();
        outline.setUniformf("size", (float) radius);
        outline.setUniformi("textureIn", 0);
        outline.setUniformi("textureToCheck", 20);
        outline.setUniformf("texelSize", 1.0F / (float) mc.getMainWindow().getWidth(), 1.0F / (float) mc.getMainWindow().getHeight());
        outline.setUniformf("direction", 1.0F, 0.0F);
        outline.setUniformf("color", colors[0], colors[1], colors[2]);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL30.GL_ONE, GL30.GL_SRC_ALPHA);
        GL30.glAlphaFunc(GL30.GL_GREATER, 0.0001f);

        inFrameBuffer.bindFramebufferTexture();
        CustomFramebuffer.drawTexture();

        mc.getFramebuffer().bindFramebuffer(false);
        GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

        outline.setUniformf("direction", 0.0F, 1.0F);

        outFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE20);
        inFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        CustomFramebuffer.drawTexture();

        outline.detach();
        GlStateManager.bindTexture(0);
        GlStateManager.disableBlend();
    }

    public static Framebuffer setupBuffer(Framebuffer frameBuffer) {
        if (frameBuffer.framebufferWidth != mc.getMainWindow().getWidth() || frameBuffer.framebufferHeight != mc.getMainWindow().getFramebufferHeight())
            frameBuffer.resize(Math.max(1, mc.getMainWindow().getWidth()), Math.max(1, mc.getMainWindow().getFramebufferHeight()), false);
        else
            frameBuffer.framebufferClear(false);
        frameBuffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);

        return frameBuffer;
    }
}
