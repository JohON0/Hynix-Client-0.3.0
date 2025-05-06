package io.hynix.utils.johon0.render.render2d;

import com.jhlabs.image.GaussianFilter;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import io.hynix.HynixMain;
import io.hynix.managers.theme.Theme;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.units.impl.miscellaneous.BetterMinecraft;
import io.hynix.units.impl.display.Hud;
import io.hynix.ui.mainmenu.MainScreen;
import io.hynix.utils.client.IMinecraft;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.johon0.math.Vector4i;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.other.Stencil;
import io.hynix.utils.johon0.render.shader.ShaderUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import net.optifine.util.TextureUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

import static com.mojang.blaze3d.platform.GlStateManager.*;
import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.systems.RenderSystem.enableBlend;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.*;
import static org.lwjgl.opengl.GL11.*;

public class RenderUtils implements IMinecraft {
    public static void quads(float x, float y, float width, float height, int glQuads, int color) {
        buffer.begin(glQuads, POSITION_TEX_COLOR);
        {
            buffer.pos(x, y, 0).tex(0, 0).color(color).endVertex();
            buffer.pos(x, y + height, 0).tex(0, 1).color(color).endVertex();
            buffer.pos(x + width, y + height, 0).tex(1, 1).color(color).endVertex();
            buffer.pos(x + width, y, 0).tex(1, 0).color(color).endVertex();
        }
        tessellator.draw();
    }

    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0);
    }
    public static float getHurtPercent(LivingEntity hurt) {
        return getRenderHurtTime(hurt) / (float) 10;
    }
    public static float getRenderHurtTime(LivingEntity hurt) {
        return (float) hurt.hurtTime - (hurt.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f);
    }

    static ShaderUtils head = new ShaderUtils("faceround");
    public static void drawHead(ResourceLocation skin,float x, float y, float width, float height, float radius, float alpha, float hurtPercent){
        mc.getTextureManager().bindTexture(skin);
        pushMatrix();
        enableBlend();
        head.attach();
        head.setUniformf("size", width,height);
        head.setUniformf("radius", radius);
        head.setUniformf("hurt_time", hurtPercent);
        head.setUniformf("alpha", alpha);

        head.setUniformf("startX", 4);
        head.setUniformf("startY", 4);

        head.setUniformf("endX", 8);
        head.setUniformf("endY", 8);

        head.setUniformf("texXSize", 32);
        head.setUniformf("texYSize", 32);
        head.drawQuads(x+2, y+2, width, height);
        head.detach();
        disableBlend();
        popMatrix();
    }

    public static void drawShader(TimerUtils timerUtility) {
        if (!BetterMinecraft.isFpsMode()) {
            for (float i=0;i<1337;i++) {
                if(timerUtility.isReached(10)){
                    MainScreen.o++;
                    i=0;
                    timerUtility.reset();
                }
            }

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();

            ShaderUtils.MainMenuShader.attach();
            ShaderUtils.MainMenuShader.setUniform("time", MainScreen.o / 40);
            ShaderUtils.MainMenuShader.setUniform("width", (float) window.getWidth());
            ShaderUtils.MainMenuShader.setUniform("height", (float) window.getHeight());

            RenderUtils.drawQuads(0, 0, window.getScaledWidth(), window.getScaledHeight(), 7);

            ShaderUtils.MainMenuShader.detach();

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        } else {
            RenderUtils.drawRoundedRect(0, 0, window.getScaledWidth(), window.getScaledHeight(), 0, ColorUtils.rgba(10,10,10,255));
        }
    }

    public static void drawCircleWithFill(float x, float y, float start, float end, float radius, float width, boolean filled, int color) {
        float sin;
        float cos;
        float i;
        if (start > end) {
            float endOffset = end;
            end = start;
            start = endOffset;
        }
        GlStateManager.enableBlend();
        GL11.glDisable(3553);
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(2848);
        GL11.glLineWidth(width);
        GL11.glBegin(3);
        for (i = end; i >= start; i -= 1.0f) {
            ColorUtils.setColor(color);
            cos = MathHelper.cos((float) ((double) i * Math.PI / 180.0)) * radius;
            sin = MathHelper.sin((float) ((double) i * Math.PI / 180.0)) * radius;
            GL11.glVertex2f(x + cos, y + sin);
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        if (filled) {
            GL11.glBegin(6);
            for (i = end; i >= start; i -= 1.0f) {
                ColorUtils.setColor(color);
                cos = MathHelper.cos((float) ((double) i * Math.PI / 180.0)) * radius;
                sin = MathHelper.sin((float) ((double) i * Math.PI / 180.0)) * radius;
                GL11.glVertex2f(x + cos, y + sin);
            }
            GL11.glEnd();
        }
        GL11.glEnable(3553);
        disableBlend();
    }

    public static void drawCircleWithFill(float x, float y, float start, float end, float radius, float width, boolean filled) {
        float sin;
        float cos;
        float i;
        if (start > end) {
            float endOffset = end;
            end = start;
            start = endOffset;
        }
        GlStateManager.enableBlend();
        RenderSystem.disableAlphaTest();
        GL11.glDisable(3553);
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.shadeModel(7425);
        GL11.glEnable(2848);
        GL11.glLineWidth(width);
        GL11.glBegin(3);
        for (i = end; i >= start; i -= 1.0f) {
            ColorUtils.setColor(ColorUtils.getColor((int) (i * 1.0f)));
            cos = MathHelper.cos((float) ((double) i * Math.PI / 180.0)) * radius;
            sin = MathHelper.sin((float) ((double) i * Math.PI / 180.0)) * radius;
            GL11.glVertex2f(x + cos, y + sin);
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        if (filled) {
            GL11.glBegin(6);
            for (i = end; i >= start; i -= 1.0f) {
                ColorUtils.setColor(ColorUtils.getColor((int) (i * 1.0f)));
                cos = MathHelper.cos((float) ((double) i * Math.PI / 180.0)) * radius;
                sin = MathHelper.sin((float) ((double) i * Math.PI / 180.0)) * radius;
                GL11.glVertex2f(x + cos, y + sin);
            }
            GL11.glEnd();
        }
        RenderSystem.enableAlphaTest();
        RenderSystem.shadeModel(7424);
        GL11.glEnable(3553);
        disableBlend();
    }

    public static boolean isInRegion(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static boolean isInRegion(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    public static boolean isInRegion(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    public static void drawStyledRect(MatrixStack matrixStack, float x, float y, float width, float height) {
        Hud hud = HynixMain.getInstance().getModuleManager().getHud();
        fancyElementRect(x, y, width, height, 140, 80);
        drawRectHorizontalW(x + 0.5f, y + 17, width - 1f, 2.5f, ColorUtils.rgba(0,0,0,0), ColorUtils.rgba(0,0,0, (int) (255 * 0.25f)));
    }

    public static void drawStyledShadowRectWithChange(MatrixStack matrixStack, float x, float y, float width, float height) {
        Hud hud = HynixMain.getInstance().getModuleManager().getHud();
    }

    public static void drawShadowFancyRect(float x, float y, float width, float height) {
        fancyRect(x,y,width,height, 140);
    }

    public static void drawShadowFancyRectNoOutline(MatrixStack matrixStack, float x, float y, float width, float height, int alpha) {
        fancyRectNoOutline(matrixStack, x, y, width, height, alpha);
    }

    public static void drawShadowFancyRectNoOutline(MatrixStack matrixStack, float x, float y, float width, float height) {
        fancyRectNoOutline(matrixStack, x, y, width, height, 180);
    }

    private static void fancyRectNoOutline(MatrixStack matrixStack, float x, float y, float width, float height, int alpha) {
        int color = ColorUtils.setAlpha(ClickGui.backgroundpanelcolor, alpha);

        RectUtils.getInstance().drawRoundedRectShadowed(matrixStack, x, y, x + width, y + height, 5, 2, color, color, color, color, false, false, true, true);
    }

    private static void fancyRect(float x, float y, float width, float height, int alpha) {
        int color = ColorUtils.reAlphaInt(Theme.darkMainRectColor, alpha);

        float off = 1.5f;

        Stencil.initStencilToWrite();
        drawRoundedRect(x, y, width, height, new Vector4f(5.5f,5.5f,5.5f,5.5f), color);
        Stencil.readStencilBuffer(0);
        drawRoundedRect(x - off, y - off, width + off * 2, height + off * 2, new Vector4f(6,6,6,6), Theme.mainRectColor);
        drawShadow(x - off, y - off, width + off * 2, height + off * 2, 8, Theme.mainRectColor);
        Stencil.uninitStencilBuffer();
        drawRoundedRect(x, y, width, height, new Vector4f(4,4,4,4), color);
        drawShadow(x, y, width, height, 8, color);
    }

    private static void fancyElementRect(float x, float y, float width, float height, int alpha, int upRectAlpha) {
        int colorWithAlpha1 = ColorUtils.reAlphaInt(Theme.darkMainRectColor, alpha);
        int colorWithAlpha2 = ColorUtils.reAlphaInt(Theme.darkMainRectColor, alpha);

        Stencil.initStencilToWrite();
        drawRoundedRect(x, y, (float) width, 17, new Vector4f(4, 0, 4, 0), -1);
        Stencil.readStencilBuffer(0);
        drawRoundedRect(x, y, width, height, new Vector4f(4,4,4,4), new Vector4i(colorWithAlpha1,colorWithAlpha1,colorWithAlpha2,colorWithAlpha2));
        Stencil.uninitStencilBuffer();
        drawShadow(x, y, width, height, 8, colorWithAlpha1, colorWithAlpha2);
        drawRoundedRect(x, y, (float) width, 17, new Vector4f(4, 0, 4, 0), ColorUtils.reAlphaInt(Theme.mainRectColor, upRectAlpha));
    }

    public static void scaleStart(float x, float y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, 0);
        GlStateManager.scaled(scale, scale, 1);
        GlStateManager.translated(-x, -y, 0);
    }

    public static void scaleEnd() {
        GlStateManager.popMatrix();
    }

    public static void scissor(double x, double y, double width, double height) {

        final double scale = mc.getMainWindow().getGuiScaleFactor();

        y = mc.getMainWindow().getScaledHeight() - y;

        x *= scale;
        y *= scale;
        width *= scale;
        height *= scale;

        GL11.glScissor((int) x, (int) (y - height), (int) width, (int) height);
    }


    private static final HashMap<Integer, Integer> shadowCache = new HashMap<Integer, Integer>();

    public static void drawShadow(float x, float y, float width, float height, int radius, int color, int i) {
        if (BetterMinecraft.isFpsMode()) return;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f);
        GlStateManager.disableAlphaTest();
        GL11.glShadeModel(7425);

        x -= radius;
        y -= radius;
        width = width + radius * 2;
        height = height + radius * 2;
        x -= 0.25f;
        y += 0.25f;

        int identifier = Objects.hash(width, height, radius);
        int textureId;

        if (shadowCache.containsKey(identifier)) {
            textureId = shadowCache.get(identifier);
            GlStateManager.bindTexture(textureId);
        } else {
            if (width <= 0) {
                width = 1;
            }

            if (height <= 0) {
                height = 1;
            }

            BufferedImage originalImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D graphics = originalImage.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(radius, radius, (int) (width - radius * 2), (int) (height - radius * 2));
            graphics.dispose();

            GaussianFilter filter = new GaussianFilter(radius);
            BufferedImage blurredImage = filter.filter(originalImage, null);
            DynamicTexture texture = new DynamicTexture(TextureUtils.toNativeImage(blurredImage));
            texture.setBlurMipmap(true, true);
            textureId = texture.getGlTextureId();
            shadowCache.put(identifier, textureId);
        }

        float[] startColorComponents = ColorUtils.rgba(color);
        float[] i1 = ColorUtils.rgba(i);
        buffer.begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
        buffer.pos(x, y, 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(0.0f, 0.0f)
                .endVertex();

        buffer.pos(x, y + (float) ((int) height), 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(0.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y + (float) ((int) height), 0.0f)
                .color(i1[0], i1[1], i1[2],
                        i1[3])
                .tex(1.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y, 0.0f)
                .color(i1[0], i1[1], i1[2],
                        i1[3])
                .tex(1.0f, 0.0f)
                .endVertex();

        tessellator.draw();
        GlStateManager.enableAlphaTest();
        GL11.glShadeModel(7424);
        GlStateManager.bindTexture(0);
        disableBlend();
    }

    public static void drawShadowVertical(float x, float y, float width, float height, int radius, int color, int i) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f);
        GlStateManager.disableAlphaTest();
        GL11.glShadeModel(7425);

        x -= radius;
        y -= radius;
        width = width + radius * 2;
        height = height + radius * 2;
        x -= 0.25f;
        y += 0.25f;

        int identifier = Objects.hash(width, height, radius);
        int textureId;

        if (shadowCache.containsKey(identifier)) {
            textureId = shadowCache.get(identifier);
            GlStateManager.bindTexture(textureId);
        } else {
            if (width <= 0) {
                width = 1;
            }

            if (height <= 0) {
                height = 1;
            }

            BufferedImage originalImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D graphics = originalImage.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(radius, radius, (int) (width - radius * 2), (int) (height - radius * 2));
            graphics.dispose();

            GaussianFilter filter = new GaussianFilter(radius);
            BufferedImage blurredImage = filter.filter(originalImage, null);
            DynamicTexture texture = new DynamicTexture(TextureUtils.toNativeImage(blurredImage));
            texture.setBlurMipmap(true, true);
            try {
                textureId = texture.getGlTextureId();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            shadowCache.put(identifier, textureId);
        }

        float[] startColorComponents = ColorUtils.rgba(color);
        float[] i1 = ColorUtils.rgba(i);
        buffer.begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
        buffer.pos(x, y, 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(0.0f, 0.0f)
                .endVertex();

        buffer.pos(x, y + (float) ((int) height), 0.0f)
                .color(i1[0], i1[1], i1[2],
                        i1[3])
                .tex(0.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y + (float) ((int) height), 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(1.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y, 0.0f)
                .color(i1[0], i1[1], i1[2],
                        i1[3])
                .tex(1.0f, 0.0f)
                .endVertex();

        tessellator.draw();
        GlStateManager.enableAlphaTest();
        GL11.glShadeModel(7424);
        GlStateManager.bindTexture(0);
        disableBlend();
    }

    public static void drawShadow(float x, float y, float width, float height, int radius, int color) {
        if (BetterMinecraft.isFpsMode()) return;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f);
        GlStateManager.disableAlphaTest();

        x -= radius;
        y -= radius;
        width = width + radius * 2;
        height = height + radius * 2;
        x -= 0.25f;
        y += 0.25f;

        int identifier = Objects.hash(width, height, radius);
        int textureId;

        if (shadowCache.containsKey(identifier)) {
            textureId = shadowCache.get(identifier);
            GlStateManager.bindTexture(textureId);
        } else {
            if (width <= 0) {
                width = 1;
            }

            if (height <= 0) {
                height = 1;
            }

            BufferedImage originalImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D graphics = originalImage.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(radius, radius, (int) (width - radius * 2), (int) (height - radius * 2));
            graphics.dispose();

            GaussianFilter filter = new GaussianFilter(radius);
            BufferedImage blurredImage = filter.filter(originalImage, null);
            DynamicTexture texture = new DynamicTexture(TextureUtils.toNativeImage(blurredImage));
            texture.setBlurMipmap(true, true);
            try {
                textureId = texture.getGlTextureId();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            shadowCache.put(identifier, textureId);
        }

        float[] startColorComponents = ColorUtils.rgba(color);

        buffer.begin(GL11.GL_QUADS, POSITION_COLOR_TEX);
        buffer.pos(x, y, 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(0.0f, 0.0f)
                .endVertex();

        buffer.pos(x, y + (float) ((int) height), 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(0.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y + (float) ((int) height), 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(1.0f, 1.0f)
                .endVertex();

        buffer.pos(x + (float) ((int) width), y, 0.0f)
                .color(startColorComponents[0], startColorComponents[1], startColorComponents[2],
                        startColorComponents[3])
                .tex(1.0f, 0.0f)
                .endVertex();

        tessellator.draw();
        GlStateManager.enableAlphaTest();
        GlStateManager.bindTexture(0);
        disableBlend();
    }

    public static void drawImage(MatrixStack stack, ResourceLocation image, double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4) {
        mc.getTextureManager().bindTexture(image);
        drawImage(stack, x, y, z, width, height, color1, color2, color3, color4);
    }
    public static void drawImage(MatrixStack stack, double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4) {
        boolean blend = glIsEnabled(GL_BLEND);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE);
        glShadeModel(GL_SMOOTH);
        glAlphaFunc(GL_GREATER, 0);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        buffer.pos(stack.getLast().getMatrix(), (float) x, (float) (y + height), (float) (z)).color((color1 >> 16) & 0xFF, (color1 >> 8) & 0xFF, color1 & 0xFF, color1 >>> 24).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
        buffer.pos(stack.getLast().getMatrix(), (float) (x + width), (float) (y + height), (float) (z)).color((color2 >> 16) & 0xFF, (color2 >> 8) & 0xFF, color2 & 0xFF, color2 >>> 24).tex(1, 1 - 0.01f).lightmap(0, 240).endVertex();
        buffer.pos(stack.getLast().getMatrix(), (float) (x + width), (float) y, (float) z).color((color3 >> 16) & 0xFF, (color3 >> 8) & 0xFF, color3 & 0xFF, color3 >>> 24).tex(1, 0).lightmap(0, 240).endVertex();
        buffer.pos(stack.getLast().getMatrix(), (float) x, (float) y, (float) z).color((color4 >> 16) & 0xFF, (color4 >> 8) & 0xFF, color4 & 0xFF, color4 >>> 24).tex(0, 0).lightmap(0, 240).endVertex();
        tessellator.draw();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        glShadeModel(GL_FLAT);
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ZERO);
        if (!blend)
            GlStateManager.disableBlend();
    }

    public static void drawImage(ResourceLocation resourceLocation, float x, float y, float width, float height,
                                 int color) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        mc.getTextureManager().bindTexture(resourceLocation);
        quads(x, y, width, height, 7, color);
        RenderSystem.shadeModel(7424);
        RenderSystem.color4f(1, 1, 1, 1);
        RenderSystem.popMatrix();

    }

    public static void drawImage(ResourceLocation resourceLocation, float x, float y, float width, float height,
                                 Vector4i color) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        mc.getTextureManager().bindTexture(resourceLocation);
        buffer.begin(7, POSITION_TEX_COLOR);
        {
            buffer.pos(x, y, 0).tex(0, 0).color(color.x).endVertex();
            buffer.pos(x, y + height, 0).tex(0, 1).color(color.y).endVertex();
            buffer.pos(x + width, y + height, 0).tex(1, 1).color(color.z).endVertex();
            buffer.pos(x + width, y, 0).tex(1, 0).color(color.w).endVertex();
        }
        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.color4f(1, 1, 1, 1);
        RenderSystem.popMatrix();

    }
    public static void drawImage(ResourceLocation resourceLocation, float x, float y, float width, float height, float radius,
                                 Vector4i color) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        mc.getTextureManager().bindTexture(resourceLocation);
        buffer.begin(7, POSITION_TEX_COLOR);

        // Рисование четырех прямоугольников и четырех углов с радиусом
        // Основной прямоугольник
        buffer.pos(x + radius, y, 0).tex(0, 0).color(color.x).endVertex();
        buffer.pos(x + width - radius, y, 0).tex(1, 0).color(color.x).endVertex();
        buffer.pos(x + width - radius, y + height, 0).tex(1, 1).color(color.y).endVertex();
        buffer.pos(x + radius, y + height, 0).tex(0, 1).color(color.y).endVertex();

        // Верхний левый угол
        drawArc(buffer, x + radius, y + radius, radius, 180, 270, color.x); // Верхний левый
        // Верхний правый угол
        drawArc(buffer, x + width - radius, y + radius, radius, 270, 360, color.y); // Верхний правый
        // Нижний левый угол
        drawArc(buffer, x + radius, y + height - radius, radius, 90, 180, color.w); // Нижний левый
        // Нижний правый угол
        drawArc(buffer, x + width - radius, y + height - radius, radius, 0, 90, color.z); // Нижний правый

        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.color4f(1, 1, 1, 1);
        RenderSystem.popMatrix();
    }

    private static void drawArc(BufferBuilder buffer, float centerX, float centerY, float radius, float startAngle, float endAngle, int color) {
        int segments = 20; // Количество сегментов для закругления
        for (int i = 0; i <= segments; i++) {
            float angle1 = (float) Math.toRadians(startAngle + (endAngle - startAngle) * i / segments);
            float angle2 = (float) Math.toRadians(startAngle + (endAngle - startAngle) * (i + 1) / segments);

            float x1 = centerX + radius * (float) Math.cos(angle1);
            float y1 = centerY + radius * (float) Math.sin(angle1);
            float x2 = centerX + radius * (float) Math.cos(angle2);
            float y2 = centerY + radius * (float) Math.sin(angle2);

            buffer.pos(centerX, centerY, 0).tex(0.5F, 0.5F).color(color).endVertex();
            buffer.pos(x1, y1, 0).tex(0, 0).color(color).endVertex();
            buffer.pos(x2, y2, 0).tex(0, 0).color(color).endVertex();
        }
    }

    public static void drawRectWBuilding(
            double left,
            double top,
            double right,
            double bottom,
            int color) {
        right += left;
        bottom += top;

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.pos(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 0.0F).color(f, f1, f2, f3).endVertex();
    }

    public static void drawRectBuilding(
            double left,
            double top,
            double right,
            double bottom,
            int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.pos(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 0.0F).color(f, f1, f2, f3).endVertex();
    }

    public static void drawMCVerticalBuilding(double x,
                                              double y,
                                              double width,
                                              double height,
                                              int start,
                                              int end) {

        float f = (float) (start >> 24 & 255) / 255.0F;
        float f1 = (float) (start >> 16 & 255) / 255.0F;
        float f2 = (float) (start >> 8 & 255) / 255.0F;
        float f3 = (float) (start & 255) / 255.0F;
        float f4 = (float) (end >> 24 & 255) / 255.0F;
        float f5 = (float) (end >> 16 & 255) / 255.0F;
        float f6 = (float) (end >> 8 & 255) / 255.0F;
        float f7 = (float) (end & 255) / 255.0F;


        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(width, height, 0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(x, y, 0f).color(f5, f6, f7, f4).endVertex();
    }

    public static void drawMCHorizontalBuilding(double x,
                                                double y,
                                                double width,
                                                double height,
                                                int start,
                                                int end) {


        float f = (float) (start >> 24 & 255) / 255.0F;
        float f1 = (float) (start >> 16 & 255) / 255.0F;
        float f2 = (float) (start >> 8 & 255) / 255.0F;
        float f3 = (float) (start & 255) / 255.0F;
        float f4 = (float) (end >> 24 & 255) / 255.0F;
        float f5 = (float) (end >> 16 & 255) / 255.0F;
        float f6 = (float) (end >> 8 & 255) / 255.0F;
        float f7 = (float) (end & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.pos(x, height, 0f).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(width, height, 0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(width, y, 0f).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(x, y, 0f).color(f1, f2, f3, f).endVertex();
    }

    public static void drawRect(
            double left,
            double top,
            double right,
            double bottom,
            int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRectW(
            double x,
            double y,
            double w,
            double h,
            int color) {

        w = x + w;
        h = y + h;

        if (x < w) {
            double i = x;
            x = w;
            w = i;
        }

        if (y < h) {
            double j = y;
            y = h;
            h = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(w, h, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(w, y, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(x, y, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRectHorizontalW(double x, double y, double w, double h, int color, int color1) {
        w = x + w;
        h = y + h;

        if (x < w) {
            double i = x;
            x = w;
            w = i;
        }

        if (y < h) {
            double j = y;
            y = h;
            h = j;
        }

        float[] colorOne = ColorUtils.rgba(color);
        float[] colorTwo = ColorUtils.rgba(color1);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.shadeModel(7425);
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
        bufferbuilder.pos(w, h, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
        bufferbuilder.pos(w, y, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
        bufferbuilder.pos(x, y, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.shadeModel(7424);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawRectVerticalW(double x, double y, double w, double h, int color, int color1) {
        w = x + w;
        h = y + h;

        if (x < w) {
            double i = x;
            x = w;
            w = i;
        }

        if (y < h) {
            double j = y;
            y = h;
            h = j;
        }

        float[] colorOne = ColorUtils.rgba(color);
        float[] colorTwo = ColorUtils.rgba(color1);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
        bufferbuilder.pos(w, h, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
        bufferbuilder.pos(w, y, 0.0F).color(colorTwo[0], colorTwo[1], colorTwo[2], colorTwo[3]).endVertex();
        bufferbuilder.pos(x, y, 0.0F).color(colorOne[0], colorOne[1], colorOne[2], colorOne[3]).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
    }

    public static void drawGradientRoundedRect(float x, float y, float w, float h, Vector4f r){
        drawRoundedRect(x, y, w, h, new Vector4f(r.x,r.y,r.z,r.w), new Vector4i(
                ColorUtils.getColor((int) (10 + System.currentTimeMillis() / 1000 % 360)),
                ColorUtils.getColor((int) (5 + System.currentTimeMillis() / 1000 % 360)),
                ColorUtils.getColor((int) (1 + System.currentTimeMillis() / 1000 % 360)),
                ColorUtils.getColor((int) (-1 + System.currentTimeMillis() / 1000 % 360)))
        );
    }

    public static void drawGradientRoundedRect(float x, float y, float w, float h, float r){
        drawRoundedRect(x, y, w, h, new Vector4f(r,r,r,r), new Vector4i(
                ColorUtils.getColor((int) (10 + System.currentTimeMillis() / 1000 % 360)),
                ColorUtils.getColor((int) (5 + System.currentTimeMillis() / 1000 % 360)),
                ColorUtils.getColor((int) (1 + System.currentTimeMillis() / 1000 % 360)),
                ColorUtils.getColor((int) (-1 + System.currentTimeMillis() / 1000 % 360)))
        );
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, Vector4i color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        ShaderUtils.smoothGradient.attach();

        ShaderUtils.smoothGradient.setUniformf("location", (float) (x * mc.getMainWindow().getGuiScaleFactor()), (float) ((mc.getMainWindow().getHeight() - (height * mc.getMainWindow().getGuiScaleFactor())) - (y * mc.getMainWindow().getGuiScaleFactor())));
        ShaderUtils.smoothGradient.setUniformf("rectSize", width * mc.getMainWindow().getGuiScaleFactor(), height * mc.getMainWindow().getGuiScaleFactor());
        ShaderUtils.smoothGradient.setUniformf("radius", radius * mc.getMainWindow().getGuiScaleFactor());
        ShaderUtils.smoothGradient.setUniform("blur", 0);

        ShaderUtils.smoothGradient.setUniform("color1", ColorUtils.rgba(color.getX()));
        ShaderUtils.smoothGradient.setUniform("color2", ColorUtils.rgba(color.getY()));
        ShaderUtils.smoothGradient.setUniform("color3", ColorUtils.rgba(color.getZ()));
        ShaderUtils.smoothGradient.setUniform("color4", ColorUtils.rgba(color.getW()));
        drawQuads(x, y, width, height, 7);

        ShaderUtils.smoothGradient.detach();
        disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawRoundedRect(float x, float y, float width, float height, Vector4f vector4f, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        ShaderUtils.rounded.attach();
        ShaderUtils.rounded.setUniform("size", width * 2, height * 2);
        ShaderUtils.rounded.setUniform("round", vector4f.x * 2, vector4f.y * 2, vector4f.z * 2, vector4f.w * 2);
        ShaderUtils.rounded.setUniform("smoothness", 0.f, 1.5f);
        ShaderUtils.rounded.setUniform("color1", ColorUtils.rgba(color));
        ShaderUtils.rounded.setUniform("color2", ColorUtils.rgba(color));
        ShaderUtils.rounded.setUniform("color3", ColorUtils.rgba(color));
        ShaderUtils.rounded.setUniform("color4", ColorUtils.rgba(color));
        drawQuads(x, y, width, height, 7);

        ShaderUtils.rounded.detach();
        disableBlend();

        GlStateManager.popMatrix();
    }

    public static void drawRoundedRect(float x, float y, float width, float height, Vector4f vector4f, Vector4i color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        ShaderUtils.rounded.attach();

        ShaderUtils.rounded.setUniform("size", width * 2, height * 2);
        ShaderUtils.rounded.setUniform("round", vector4f.x * 2, vector4f.y * 2, vector4f.z * 2, vector4f.w * 2);

        ShaderUtils.rounded.setUniform("smoothness", 0.f, 1.5f);
        ShaderUtils.rounded.setUniform("color1", ColorUtils.rgba(color.getX()));
        ShaderUtils.rounded.setUniform("color2", ColorUtils.rgba(color.getY()));
        ShaderUtils.rounded.setUniform("color3", ColorUtils.rgba(color.getZ()));
        ShaderUtils.rounded.setUniform("color4", ColorUtils.rgba(color.getW()));
        drawQuads(x, y, width, height, 7);

        ShaderUtils.rounded.detach();
        disableBlend();
        GlStateManager.popMatrix();
    }


    public static void drawRoundedRect(float x, float y, float width, float height, float outline, int color1, Vector4f vector4f, Vector4i color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        ShaderUtils.roundedout.attach();

        ShaderUtils.roundedout.setUniform("size", width * 2, height * 2);
        ShaderUtils.roundedout.setUniform("round", vector4f.x * 2, vector4f.y * 2, vector4f.z * 2, vector4f.w * 2);

        ShaderUtils.roundedout.setUniform("smoothness", 0.f, 1.5f);
        ShaderUtils.roundedout.setUniform("outlineColor", ColorUtils.rgba(color.getX()));
        ShaderUtils.roundedout.setUniform("outlineColor1", ColorUtils.rgba(color.getY()));
        ShaderUtils.roundedout.setUniform("outlineColor2", ColorUtils.rgba(color.getZ()));
        ShaderUtils.roundedout.setUniform("outlineColor3", ColorUtils.rgba(color.getW()));
        ShaderUtils.roundedout.setUniform("color", ColorUtils.rgba(color1));
        ShaderUtils.roundedout.setUniform("outline", outline);
        drawQuads(x, y, width, height, 7);

        ShaderUtils.rounded.detach();
        disableBlend();
        GlStateManager.popMatrix();
    }

    private static Framebuffer whiteCache = new Framebuffer(1, 1, false, true);
    private static Framebuffer contrastCache = new Framebuffer(1, 1, false, true);

    public static void drawContrast(float state) {
        state = MathHelper.clamp(state, 0, 1);
        GlStateManager.enableBlend();
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        contrastCache = ShaderUtils.createFrameBuffer(contrastCache);

        contrastCache.framebufferClear(false);
        contrastCache.bindFramebuffer(true);

        // prepare image
        ShaderUtils.contrast.attach();
        ShaderUtils.contrast.setUniform("texture", 0);
        ShaderUtils.contrast.setUniformf("contrast", state);
        GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);

        ShaderUtils.drawQuads();
        contrastCache.unbindFramebuffer();
        ShaderUtils.contrast.detach();
        mc.getFramebuffer().bindFramebuffer(true);

        // draw image
        ShaderUtils.contrast.attach();
        ShaderUtils.contrast.setUniform("texture", 0);
        ShaderUtils.contrast.setUniformf("contrast", state);
        GlStateManager.bindTexture(contrastCache.framebufferTexture);
        ShaderUtils.drawQuads();
        ShaderUtils.contrast.detach();

        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.bindTexture(0);
    }

    public static void drawWhite(float state) {
        state = MathHelper.clamp(state, 0, 1);
        GlStateManager.enableBlend();
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        whiteCache = ShaderUtils.createFrameBuffer(whiteCache);

        whiteCache.framebufferClear(false);
        whiteCache.bindFramebuffer(true);

        // prepare image
        ShaderUtils.white.attach();
        ShaderUtils.white.setUniform("texture", 0);
        ShaderUtils.white.setUniformf("state", state);
        GlStateManager.bindTexture(mc.getFramebuffer().framebufferTexture);

        ShaderUtils.drawQuads();
        whiteCache.unbindFramebuffer();
        ShaderUtils.white.detach();
        mc.getFramebuffer().bindFramebuffer(true);

        // draw image
        ShaderUtils.white.attach();
        ShaderUtils.white.setUniform("texture", 0);
        ShaderUtils.white.setUniformf("state", state);
        GlStateManager.bindTexture(whiteCache.framebufferTexture);
        ShaderUtils.drawQuads();
        ShaderUtils.white.detach();

        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.bindTexture(0);
    }


    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        ShaderUtils.smooth.attach();

        ShaderUtils.smooth.setUniformf("location", (float) (x * mc.getMainWindow().getGuiScaleFactor()), (float) ((mc.getMainWindow().getHeight() - (height * mc.getMainWindow().getGuiScaleFactor())) - (y * mc.getMainWindow().getGuiScaleFactor())));
        ShaderUtils.smooth.setUniformf("rectSize", width * mc.getMainWindow().getGuiScaleFactor(), height * mc.getMainWindow().getGuiScaleFactor());
        ShaderUtils.smooth.setUniformf("radius", radius * mc.getMainWindow().getGuiScaleFactor());
        ShaderUtils.smooth.setUniform("blur", 0);
        ShaderUtils.smooth.setUniform("color", ColorUtils.rgba(color));
        drawQuads(x, y, width, height, 7);

        ShaderUtils.smooth.detach();
        disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawCircle(float x, float y, float radius, int color) {
        drawRoundedRect(x - radius / 2f, y - radius / 2f, radius, radius, radius / 2f, color);
    }

    public static void drawShadowCircle(float x, float y, float radius, int color) {
        drawShadow(x - radius / 2f, y - radius / 2f, radius, radius, (int) radius, color);
    }

    public static void drawQuads(float x, float y, float width, float height, int glQuads) {
        buffer.begin(glQuads, POSITION_TEX);
        {
            buffer.pos(x, y, 0).tex(0, 0).endVertex();
            buffer.pos(x, y + height, 0).tex(0, 1).endVertex();
            buffer.pos(x + width, y + height, 0).tex(1, 1).endVertex();
            buffer.pos(x + width, y, 0).tex(1, 0).endVertex();
        }
        Tessellator.getInstance().draw();
    }

    public static void drawBox(double x, double y, double width, double height, double size, int color) {
        drawRectBuilding(x + size, y, width - size, y + size, color);
        drawRectBuilding(x, y, x + size, height, color);

        drawRectBuilding(width - size, y, width, height, color);
        drawRectBuilding(x + size, height - size, width - size, height, color);
    }

    public static void drawBox(double x, double y, double width, double height, double size, Vector4i colors) {
        drawMCHorizontalBuilding(x + size, y, width - size, y + size, colors.x, colors.z);
        drawMCVerticalBuilding(x, y, x + size, height, colors.z, colors.x);

        drawMCVerticalBuilding(width - size, y, width, height, colors.x, colors.z);
        drawMCHorizontalBuilding(x + size, height - size, width - size, height, colors.z, colors.x);
    }

    public static void drawBox(AxisAlignedBB bb, int color, float width) {
        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glEnable(2848);
        GL11.glLineWidth(width);
        float[] rgb = ColorUtils.rgba(color);
        GlStateManager.color4f(rgb[0], rgb[1], rgb[2], rgb[3]);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        tessellator.draw();
        vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        tessellator.draw();
        vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        tessellator.draw();
        GlStateManager.color4f(rgb[0], rgb[1], rgb[2], rgb[3]);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glPopMatrix();
    }

    public static void drawImageMultiColor(MatrixStack stack, ResourceLocation image, double x, double y, double z, double width, double height, int color1, int color2, int color3, int color4) {
        Minecraft minecraft = Minecraft.getInstance();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE);
        glShadeModel(GL_SMOOTH);
        glAlphaFunc(GL_GREATER, 0);
        minecraft.getTextureManager().bindTexture(image);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL_QUADS, POSITION_COLOR_TEX_LIGHTMAP);
        bufferBuilder.pos(stack.getLast().getMatrix(), (float) x, (float) (y + height), (float) (z)).color((color1 >> 16) & 0xFF, (color1 >> 8) & 0xFF, color1 & 0xFF, color1 >>> 24).tex(0, 1 - 0.01f).lightmap(0, 240).endVertex();
        bufferBuilder.pos(stack.getLast().getMatrix(), (float) (x + width), (float) (y + height), (float) (z)).color((color2 >> 16) & 0xFF, (color2 >> 8) & 0xFF, color2 & 0xFF, color2 >>> 24).tex(1, 1 - 0.01f).lightmap(0, 240).endVertex();
        bufferBuilder.pos(stack.getLast().getMatrix(), (float) (x + width), (float) y, (float) z).color((color3 >> 16) & 0xFF, (color3 >> 8) & 0xFF, color3 & 0xFF, color3 >>> 24).tex(1, 0).lightmap(0, 240).endVertex();
        bufferBuilder.pos(stack.getLast().getMatrix(), (float) x, (float) y, (float) z).color((color4 >> 16) & 0xFF, (color4 >> 8) & 0xFF, color4 & 0xFF, color4 >>> 24).tex(0, 0).lightmap(0, 240).endVertex();

        tessellator.draw();
        disableBlend();
    }

    public static void drawImageAlpha(ResourceLocation resourceLocation, float x, float y, float width, float height, int color) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        mc.getTextureManager().bindTexture(resourceLocation);
        quads(x, y, width, height, 7, color);
        RenderSystem.shadeModel(7424);
        RenderSystem.color4f(1, 1, 1, 1);
        RenderSystem.popMatrix();
    }


    public static void drawImageAlpha(ResourceLocation resourceLocation, float x, float y, float width, float height, Vector4i color) {
        RenderSystem.pushMatrix();
        RenderSystem.disableLighting();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableCull();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(770, 1, 0, 1);
        mc.getTextureManager().bindTexture(resourceLocation);
        buffer.begin(7, POSITION_TEX_COLOR);
        {
            buffer.pos(x, y, 0).tex(0, 1 - 0.01f).lightmap(0, 240).color(color.x).endVertex();
            buffer.pos(x, y + height, 0).tex(1, 1 - 0.01f).lightmap(0, 240).color(color.y).endVertex();
            buffer.pos(x + width, y + height, 0).tex(1, 0).lightmap(0, 240).color(color.z).endVertex();
            buffer.pos(x + width, y, 0).tex(0, 0).lightmap(0, 240).color(color.w).endVertex();

        }
        tessellator.draw();
        RenderSystem.defaultBlendFunc();
        disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableAlphaTest();
        RenderSystem.depthMask(true);
        RenderSystem.popMatrix();
    }

    @UtilityClass
    public static class FrameBuffer {
        public Framebuffer createFrameBuffer(Framebuffer framebuffer) {
            return createFrameBuffer(framebuffer, false);
        }

        public Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
            if (needsNewFramebuffer(framebuffer)) {
                if (framebuffer != null) {
                    framebuffer.deleteFramebuffer();
                }
                int frameBufferWidth = mc.getMainWindow().getFramebufferWidth();
                int frameBufferHeight = mc.getMainWindow().getFramebufferHeight();
                return new Framebuffer(frameBufferWidth, frameBufferHeight, depth);
            }
            return framebuffer;
        }

        public boolean needsNewFramebuffer(Framebuffer framebuffer) {
            return framebuffer == null || framebuffer.framebufferWidth != mc.getMainWindow().getFramebufferWidth() || framebuffer.framebufferHeight != mc.getMainWindow().getFramebufferHeight();
        }
    }
}