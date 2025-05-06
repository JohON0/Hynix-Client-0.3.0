package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.hynix.HynixMain;
import io.hynix.events.impl.EventPreRender3D;
import io.hynix.events.impl.EventRender2D;
import io.hynix.events.impl.EventUpdate;
import io.hynix.managers.theme.Theme;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.impl.DecelerateAnimation;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.math.Vector4i;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.ProjectionUtils;
import io.hynix.utils.johon0.render.render2d.RectUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.systems.RenderSystem.depthMask;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;

/**
 * @author JohON0
 * @date 12.01.2025
 */
@UnitRegister(name = "TargetESP", category = Category.Display, desc = "Отображает цель")
public class TargetESP extends Unit {

    final ModeSetting mode = new ModeSetting("Мод", "Маркер", "Маркер", "Призраки", "Кругляшок");
    //красива
    private final AnimationUtils alpha = new DecelerateAnimation(600, 255);
    //таргет
    private LivingEntity currentTarget;
    //время
    private long lastTime = System.currentTimeMillis();

    public TargetESP() {
        addSettings(mode);
    }

    //update na targeta
    @Subscribe
    private void onUpdate(EventUpdate e) {
        boolean bl = (HynixMain.getInstance().getModuleManager().getAttackAura().isEnabled());
        if (HynixMain.getInstance().getModuleManager().getAttackAura().target != null) {
            currentTarget = HynixMain.getInstance().getModuleManager().getAttackAura().target;
        }

        this.alpha.setDirection(bl && HynixMain.getInstance().getModuleManager().getAttackAura().target != null ? Direction.FORWARDS : Direction.BACKWARDS);
    }
    //render 3d esp
    @Subscribe
    public void onRender(EventPreRender3D e) {
        if (this.alpha.finished(Direction.BACKWARDS)) {
            return;
        }

        if (mode.is("Кругляшок")) {
            drawCircleMarker(e.getMatrix(), e);
        }

        if (mode.is("Призраки")) {
            drawSoulsMarker(e.getMatrix(), e);
        }
    }
    //2d render (image)
    @Subscribe
    public void onDisplay(EventRender2D e) {
        if (mode.is("Маркер")) {
            drawImageMarker(e);
        }
    }

    //scale
    public double getScale(Vector3d position, double size) {
        Vector3d cam = mc.getRenderManager().info.getProjectedView();
        double distance = cam.distanceTo(position);
        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        return Math.max(10f, 1000 / distance) * (size / 30f) / (fov == 70 ? 1 : fov / 70.0f);
    }
    //otrisovka markera
    public void drawImageMarker(EventRender2D e) {
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            double sin = Math.sin(System.currentTimeMillis() / 1000.0);
            double distance = mc.player.getDistance(currentTarget);
            float maxSize = (float) getScale(currentTarget.getPositionVec(), 10);
            float size = Math.max(maxSize - (float)distance, 20.0F);
            Vector3d interpolated = currentTarget.getPositon(e.getPartialTicks());
            Vector2f pos = ProjectionUtils.project(interpolated.x, interpolated.y + currentTarget.getHeight() / 2f, interpolated.z);
            GlStateManager.pushMatrix();
            GlStateManager.translatef(pos.x, pos.y, 0);
            GlStateManager.rotatef((float) sin * 360, 0, 0, 1);
            GlStateManager.translatef(-pos.x, -pos.y, 0);

            if (pos != null) {
                RenderUtils.drawImageAlpha(new ResourceLocation("hynix/images/modules/target.png"), pos.x - size / 2f, pos.y - size / 2f, size, size, new Vector4i(
                        ColorUtils.setAlpha(Theme.mainRectColor, (int)(alpha.getOutput())),
                        ColorUtils.setAlpha(Theme.mainRectColor, (int) (alpha.getOutput())),
                        ColorUtils.setAlpha(Theme.mainRectColor, (int)(alpha.getOutput())),
                        ColorUtils.setAlpha(Theme.mainRectColor, (int)(alpha.getOutput()))
                ));
                GlStateManager.popMatrix();
            }
        }
    }
    //prizraki pasterov
    public void drawSoulsMarker(MatrixStack stack, EventPreRender3D e) {
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            MatrixStack ms = stack;
            ms.push();

            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);

            double x = currentTarget.getPosX();
            double y = currentTarget.getPosY() + (currentTarget.getHeight() / 2f);
            double z = currentTarget.getPosZ();
            double radius = 0.35 + currentTarget.getWidth() / 2;
            float speed = 20;
            float size = 0.6f;
            double distance = 24;
            int length = 24;
            int color = ColorUtils.multAlpha(Theme.rectColor, 1);
            int alpha = 1;
            ActiveRenderInfo camera = mc.getRenderManager().info;
            ms.translate(-mc.getRenderManager().info.getProjectedView().getX(), -mc.getRenderManager().info.getProjectedView().getY(), -mc.getRenderManager().info.getProjectedView().getZ());
            Vector3d interpolated = MathUtils.interpolate(currentTarget.getPositionVec(), new Vector3d(currentTarget.lastTickPosX, currentTarget.lastTickPosY, currentTarget.lastTickPosZ), e.getPartialTicks());
            interpolated.y += 0.25 + currentTarget.getHeight() / 2;

            ms.translate(interpolated.x + 0.2, interpolated.y, interpolated.z);

            RectUtils.bindTexture(new ResourceLocation("hynix/images/glow.png"));

            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                double angle = 0.05f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle * (Math.PI / 2)) * radius;
                double c = Math.cos(angle * (Math.PI / 2)) * radius;
                double o = Math.cos(angle * (Math.PI / 3)) * radius;
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

                ms.translate(-s, o, -c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(1, 0).endVertex();

                tessellator.draw();

                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                ms.translate(s, -o, c);
            }

            for (int i = 0; i < length; i++) {
                Quaternion r = camera.getRotation().copy();
                double angle = 0.05f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                double s = Math.sin(angle * (Math.PI / 2)) * radius;
                double c = Math.cos(angle * (Math.PI / 2)) * radius;
                double o = Math.sin(angle * (Math.PI / 3)) * radius;
                buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

                ms.translate(s, o, c);
                ms.translate(-size / 2f, -size / 2f, 0);
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(0, 0).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(0, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(1, 1).endVertex();
                buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtils.reAlphaInt(color, (int) (alpha * this.alpha.getOutput()))).tex(1, 0).endVertex();

                tessellator.draw();

                ms.translate(-size / 2f, -size / 2f, 0);
                r.conjugate();
                ms.rotate(r);
                ms.translate(size / 2f, size / 2f, 0);

                ms.translate(-s, -o, -c);
            }

            ms.translate(-x, -y, -z);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            depthMask(true);
            RenderSystem.popMatrix();
            ms.pop();
        }
    }
    //krug (pasta)
    public void drawCircleMarker(MatrixStack stack, EventPreRender3D e) {
        if (this.currentTarget != null && this.currentTarget != mc.player) {
            MatrixStack ms = stack;
            ms.push();
            RenderSystem.pushMatrix();
            RenderSystem.disableLighting();
            depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 1, 0, 1);
            double radius = 0.4 + currentTarget.getWidth() / 2;
            float speed = 30;
            float size = 0.3f;
            double distance = 155;
            int lenght = (int) (distance + currentTarget.getWidth());
            ActiveRenderInfo camera = mc.getRenderManager().info;
            ms.translate(-mc.getRenderManager().info.getProjectedView().getX(),
                    -mc.getRenderManager().info.getProjectedView().getY(),
                    -mc.getRenderManager().info.getProjectedView().getZ());

            Vector3d interpolated = MathUtils.interpolate(currentTarget.getPositionVec(), new Vector3d(currentTarget.lastTickPosX, currentTarget.lastTickPosY, currentTarget.lastTickPosZ), e.getPartialTicks());
            ms.translate(interpolated.x + 0.15, interpolated.y + 0.2 + currentTarget.getHeight() / 2, interpolated.z);
            RectUtils.bindTexture(new ResourceLocation("hynix/images/glow.png"));
            for (int j = 0; j < 1; j++) {
                for (int i = 0; i < lenght; i++) {
                    Quaternion r = camera.getRotation().copy();
                    buffer.begin(GL_QUADS, POSITION_COLOR_TEX);

                    double angle = 0.1f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);

                    double s = Math.sin(angle + j * (Math.PI / 1.5)) * radius;
                    double c = Math.cos(angle + j * (Math.PI / 1.5)) * radius;

                    double yOffset = Math.sin(System.currentTimeMillis() * 0.003 + j) * 0.8;

                    ms.translate(0, yOffset, 0);

                    ms.translate(s, 0, -c);

                    ms.translate(-size / 2f, -size / 2f, 0);
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    int color = ColorUtils.getColor(i);
                    int alpha = (int) (1 * this.alpha.getOutput());
                    buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(ColorUtils.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                    tessellator.draw();

                    ms.translate(-size / 2f, -size / 2f, 0);
                    r.conjugate();
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    ms.translate(-s, 0, c);
                    ms.translate(0, -yOffset, 0);
                }
            }

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableAlphaTest();
            depthMask(true);
            RenderSystem.popMatrix();
            ms.pop();
        }
    }
}
