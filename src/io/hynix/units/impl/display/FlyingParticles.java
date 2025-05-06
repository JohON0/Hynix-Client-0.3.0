package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.hynix.HynixMain;
import io.hynix.events.impl.EventChangeWorld;
import io.hynix.events.impl.EventPreRender3D;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.johon0.animations.easing.CompactAnimation;
import io.hynix.utils.johon0.animations.easing.Easing;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.BlockUtils;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RectUtils;
import lombok.Getter;
import net.minecraft.block.*;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.joml.Math;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
/**
 * @author JohON0
 */
@UnitRegister(name = "FlyingParticles", category = Category.Display,desc = "Летающие частицы вокруг мира")
public class FlyingParticles extends Unit {
    private final List<FireFlyEntity> particles = new ArrayList<>();
    private final ModeSetting fallModeSetting = new ModeSetting("Режим", "Простой", "Простой", "Взлет");
    private final SliderSetting count = new SliderSetting("Количество", 100, 10, 1000, 10);
    public final BooleanSetting randomColor = new BooleanSetting("Рандомный цвет", false);
    private final ResourceLocation texture = new ResourceLocation("hynix/images/firefly.png");

    public FlyingParticles() {
        addSettings(fallModeSetting, count, randomColor);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        particles.clear();
        spawnParticle(mc.player);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        particles.clear();
    }

    private void spawnParticle(LivingEntity entity) {
        double distance = MathUtils.random(5, 50), yawRad = Math.toRadians(MathUtils.random(0, 360)), xOffset = -Math.sin(yawRad) * distance, zOffset = Math.cos(yawRad) * distance;

        particles.add(new FireFlyEntity(new Vector3d(entity.getPosX() + xOffset, entity.getPosY() + (fallModeSetting.is("Взлет") ? MathUtils.random(-5, 0) : MathUtils.random(3, 15)), entity.getPosZ() + zOffset),
                new Vector3d(), particles.size(), ColorUtils.random().hashCode()));
    }

    @Subscribe
    public void onChange(EventChangeWorld e) {
        particles.clear();
    }

    @Subscribe
    public void onPreRender(EventPreRender3D event) {
        ClientPlayerEntity player = mc.player;

        // Remove expired or distant particles
        particles.removeIf(particle ->
                particle.time.isReached(5000) ||
                        particle.position.distance(player.getPosX(), player.getPosY(), player.getPosZ()) >= 60
        );

        // Spawn new particles if needed
        if (particles.size() <= count.getValue().intValue() * 5) {
            spawnParticle(player);
        }

        MatrixStack matrix = event.getMatrix();
        boolean lightEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);

        RenderSystem.pushMatrix();
        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        if (lightEnabled) {
            RenderSystem.disableLighting();
        }

        GL11.glShadeModel(GL11.GL_SMOOTH);
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        RectUtils.bindTexture(texture);

        if (!particles.isEmpty()) {
            particles.forEach(fireFlyEntity -> fireFlyEntity.update(true));

            float pos = 0.5F;
            for (FireFlyEntity particle : particles) {
                updateParticleAlpha(particle);

                int alpha = (int) Math.clamp(0, (int) (particle.getAlpha().getValue()), particle.getAngle());
                int colorGlow = randomColor.getValue() ?
                        ColorUtils.reAlphaInt(particle.getColor(), alpha) :
                        ColorUtils.reAlphaInt(ColorUtils.getColor(particle.index * 250), alpha);

                renderParticle(matrix, particle, pos, alpha, colorGlow);
            }
        }

        cleanupRenderState(lightEnabled, matrix);
    }

    private void updateParticleAlpha(FireFlyEntity particle) {
        if ((int) particle.getAlpha().getValue() != 255 && !particle.time.isReached(particle.alpha.getDuration())) {
            particle.getAlpha().run(255);
        }
        if ((int) particle.getAlpha().getValue() != 0 && particle.time.isReached(8000 - particle.alpha.getDuration())) {
            particle.getAlpha().run(0);
        }
    }

    private void renderParticle(MatrixStack matrix, FireFlyEntity particle, float pos, int alpha, int colorGlow) {
        final Vector3d vec = particle.getPosition();
        matrix.push();
        RectUtils.setupOrientationMatrix(matrix, (float) vec.x, (float) vec.y, (float) vec.z);
        matrix.rotate(mc.getRenderManager().getCameraOrientation());
        matrix.translate(0, pos / 2F, 0);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RectUtils.drawRect(matrix, -pos, -pos, pos, pos, colorGlow, colorGlow, colorGlow, colorGlow, true, true);

        float size = pos / 2F;
        int color = ColorUtils.reAlphaInt(-1, alpha);
        RectUtils.drawRect(matrix, -size, -size, size, size, color, color, color, color, true, true);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        matrix.pop();
    }

    private void cleanupRenderState(boolean lightEnabled, MatrixStack matrix) {
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        GlStateManager.clearCurrentColor();
        GL11.glShadeModel(GL11.GL_FLAT);

        if (lightEnabled) {
            RenderSystem.enableLighting();
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableAlphaTest();
        matrix.pop();
        RenderSystem.popMatrix();
    }


    @Getter
    private static class FireFlyEntity {
        private final int index;
        private final TimerUtils time = new TimerUtils();
        private final CompactAnimation alpha = new CompactAnimation(Easing.LINEAR, 150);
        private final int color;

        public final Vector3d position;
        private final Vector3d delta;

        public FireFlyEntity(final Vector3d position, final Vector3d velocity, final int index, int color) {
            this.position = position;
            this.delta = new Vector3d(velocity.x, velocity.y, velocity.z);
            this.index = index;
            this.color = color;
            this.time.reset();
        }

        public void update(boolean physics) {
            if (physics) {
                final Block block1 = BlockUtils.getBlock(this.position.x, this.position.y, this.position.z + this.delta.z);
                if (isValidBlock(block1))
                    this.delta.z *= -0.8;

                final Block block2 = BlockUtils.getBlock(this.position.x, this.position.y + this.delta.y, this.position.z);
                if (isValidBlock(block2)) {
                    this.delta.x *= 0.999F;
                    this.delta.z *= 0.999F;
                    this.delta.y *= -0.7;
                }

                final Block block3 = BlockUtils.getBlock(this.position.x + this.delta.x, this.position.y, this.position.z);
                if (isValidBlock(block3))
                    this.delta.x *= -0.8;
            }

            this.updateMotion();
        }

        private boolean isValidBlock(Block block) {
            return !(block instanceof AirBlock)
                    && !(block instanceof BushBlock)
                    && !(block instanceof AbstractButtonBlock)
                    && !(block instanceof TorchBlock)
                    && !(block instanceof LeverBlock)
                    && !(block instanceof AbstractPressurePlateBlock)
                    && !(block instanceof CarpetBlock)
                    && !(block instanceof FlowingFluidBlock);
        }

        public int getAngle() {
            return (int) Math.clamp(0, 255, ((Math.sin(time.getTime() / 250D) + 1F) / 2F) * 255);
        }

        public void updateMotion() {
            // Учитываем влияние гравитации
            double gravity = 0.01; // Увеличьте это значение для более выраженной гравитации
            this.delta.y -= gravity;

            double motion = 0.005;
            this.delta.x += (Math.random() - 0.5) * motion;
            this.delta.z += (Math.random() - 0.5) * motion;

            if (!HynixMain.getInstance().getModuleManager().getFireFly().fallModeSetting.is("Простой")) {
                this.delta.y = Math.min(this.delta.y + (Math.random() * 0.02), 0.2); // Увеличиваем положительное ускорение вниз
            }

            // Регулируем скорость
            double maxSpeed = 0.3; // Уменьшаем максимальную скорость для более медленного движения
            this.delta.x = MathHelper.clamp(this.delta.x, -maxSpeed, maxSpeed);
            this.delta.y = MathHelper.clamp(this.delta.y, -maxSpeed, maxSpeed);
            this.delta.z = MathHelper.clamp(this.delta.z, -maxSpeed, maxSpeed);

        }
    }
}
