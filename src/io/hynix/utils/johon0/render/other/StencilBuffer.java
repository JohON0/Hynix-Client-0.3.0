package io.hynix.utils.johon0.render.other;

import com.mojang.blaze3d.platform.GlStateManager;
import io.hynix.utils.client.IMinecraft;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;

@UtilityClass
public class StencilBuffer implements IMinecraft {

    private void recreate(final Framebuffer framebuffer) {
        GL30.glDeleteRenderbuffers(framebuffer.depthBuffer);
        final int depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_STENCIL, mc.getMainWindow().getWidth(), mc.getMainWindow().getHeight());
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
    }

    public void checkSetupFBO(final Framebuffer framebuffer) {
        if (framebuffer != null) {
            if (framebuffer.depthBuffer > -1) {
                recreate(framebuffer);
                framebuffer.depthBuffer = -1;
            }
        }
    }

    public void init() {
        mc.getFramebuffer().bindFramebuffer(false);
        checkSetupFBO(mc.getFramebuffer());
        glClear(GL_STENCIL_BUFFER_BIT);
        glEnable(GL_STENCIL_TEST);

        glStencilFunc(GL_ALWAYS, 1, 1);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glColorMask(false, false, false, false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
    }

    public void bind() {
        glStencilFunc(GL_ALWAYS, 1, 1);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glColorMask(false, false, false, false);
    }

    public void read(final int ref) {
        glColorMask(true, true, true, true);
        glStencilFunc(GL_EQUAL, ref, 1);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
    }

    public void cleanup() {
        glDisable(GL_STENCIL_TEST);
        GlStateManager.disableAlphaTest();
        GlStateManager.disableBlend();
    }

    @Getter
    @AllArgsConstructor
    public enum Action {
        OUTSIDE(1),
        INSIDE(2);

        private final int action;
    }

}
