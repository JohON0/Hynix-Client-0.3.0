package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.hynix.HynixMain;
import io.hynix.events.impl.*;
import io.hynix.managers.premium.PremiumChecker;
import io.hynix.managers.theme.Theme;
import io.hynix.ui.notifications.impl.WarningNotify;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeListSetting;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
/**
 * @author JohON0
 * @date 15.10.24
 */
@UnitRegister(name = "Particles", category = Category.Display,desc = "Создает Частицы при каком то действии")
public class Particles extends Unit {
    private final ModeSetting textureMode = new ModeSetting("Текстура", "Bloom", "Bloom", "Конфетка");

    private final ModeListSetting elements = new ModeListSetting("Триггер",
            new BooleanSetting("Удар", true),
            new BooleanSetting("Ходьба", true),
            new BooleanSetting("Бросаемый предмет", true),
            new BooleanSetting("Прыжок", true)
    );
    public final SliderSetting sizeOfParticles = new SliderSetting("Количество при атаке", 15, 3, 50, 1).setVisible(() -> elements.is("Удар").getValue());
    public final SliderSetting sizeOfParticlesWhenWalk = new SliderSetting("Количество при ходьбе", 3, 1, 5, 1).setVisible(() -> elements.is("Ходьба").getValue());
    public final BooleanSetting randomColor = new BooleanSetting("Рандомный цвет", false);

    private final List<Particle3D> targetParticles = new ArrayList<>();
    private final List<Particle3D> flameParticles = new ArrayList<>();

    private ResourceLocation texture() {
        int r =  MathUtils.randomInt(1, 12);
        if (textureMode.is("Bloom")) {
            return new ResourceLocation("hynix/images/firefly.png");
        } else if (textureMode.is("Конфетка")) {
            return new ResourceLocation("hynix/images/" + "konfetka" + ".png");
        } else {
            return new ResourceLocation("hynix/images/modules/" + "lol" + ".png");
        }
    }
    public Particles() {
        addSettings(textureMode, elements, sizeOfParticles, sizeOfParticlesWhenWalk, randomColor);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (!PremiumChecker.isPremium && textureMode.is("Конфетка")) {
            toggle();
            HynixMain.getInstance().getNotifyManager().add(new WarningNotify("Мод " + textureMode.getValue() + " работает только для " + TextFormatting.GOLD + "премиум " + TextFormatting.WHITE + "пользователей!", 5000));
            print("Предупреждение: Мод " + textureMode.getValue() + " работает только для премиум пользователей! Если хочешь подержать проект, то премиум-подписку можно преобрести на сайте https://hynix.fun/");
        }
        targetParticles.clear();
        flameParticles.clear();
    }

    @Subscribe
    public void onAttack(AttackEvent event) {
        Entity target = event.entity;
        if (elements.is("Удар").getValue()) {
            for (int i = 0; i < sizeOfParticles.getValue(); i++) {
                // Генерация случайного вектора скорости
                Vector3d velocity = new Vector3d(
                        MathUtils.random(-3, 3), // Случайное значение по оси X
                        MathUtils.random(1, 3),  // Вверх по оси Y
                        MathUtils.random(-3, 3)   // Случайное значение по оси Z
                );
                targetParticles.add(new Particle3D(
                        new Vector3d(target.getPosX(), target.getPosY() + MathUtils.random(0, target.getHeight()), target.getPosZ()),
                        velocity, // Используем новый вектор скорости
                        targetParticles.size(),
                        ColorUtils.random().hashCode()
                ));
            }
        }
    }

    @Subscribe
    public void onJump(EventJump e) {
        LivingEntity target = mc.player;
        if (elements.is("Прыжок").getValue()) {
            for (int i = 0; i < sizeOfParticles.getValue(); i++) {
                // Генерация случайного вектора скорости
                Vector3d velocity = new Vector3d(
                        MathUtils.random(-3, 3), // Случайное значение по оси X
                        MathUtils.random(1, 4),   // Вверх по оси Y
                        MathUtils.random(-3, 3)    // Случайное значение по оси Z
                );
                flameParticles.add(new Particle3D(
                        new Vector3d(target.getPosX(), target.getPosY(), target.getPosZ()),
                        velocity, // Используем новый вектор скорости
                        flameParticles.size(),
                        ColorUtils.random().hashCode()
                ));
            }
        }
    }


    @Subscribe
    public void onMotion(EventMotion e) {
        if (elements.is("Ходьба").getValue()) {
            if (mc.player.lastTickPosX != mc.player.getPosX() || mc.player.lastTickPosY != mc.player.getPosY() || mc.player.lastTickPosZ != mc.player.getPosZ()) {
                for (int i = 0; i < sizeOfParticlesWhenWalk.getValue(); i++) {
                    flameParticles.add(new Particle3D(new Vector3d(mc.player.getPosX() + MathUtils.random(-0.5f, 0.5f), mc.player.getPosY() + MathUtils.random(0.4f, mc.player.getHeight() / 2), mc.player.getPosZ() + MathUtils.random(-0.5f, 0.5f)), new Vector3d(MathUtils.random(-0.1f, 0.1f), 0, MathUtils.random(-0.1f, 0.1f)).mul(2 * (1 + Math.random())), flameParticles.size(), ColorUtils.random().hashCode()));
                }
            }
        }

        if (elements.is("Бросаемый предмет").getValue()) {
            for (Entity entity : mc.world.getAllEntities()) {
                if (entity instanceof ThrowableEntity p) {
                    for (int i = 0; i < 3; i++) {
                        flameParticles.add(new Particle3D(
                                new Vector3d(p.getPosX() + MathUtils.random(-0.5f, 0.5f),
                                        p.getPosY() + MathUtils.random(0, p.getHeight()),
                                        p.getPosZ() + MathUtils.random(-0.5f, 0.5f)),
                                new Vector3d(MathUtils.random(-0.5f, 0.5f),
                                        MathUtils.random(-0.3f, 0.3f),
                                        MathUtils.random(-0.5f, 0.5f)).mul(2 * (1 + Math.random())),
                                flameParticles.size(),
                                ColorUtils.random().hashCode()));
                    }
                } else if (entity instanceof ArrowEntity arrow) {
                    for (int i = 0; i < 3; i++) {
                        flameParticles.add(new Particle3D(
                                new Vector3d(arrow.getPosX() + MathUtils.random(-0.5f, 0.5f),
                                        arrow.getPosY() + MathUtils.random(0, arrow.getHeight()),
                                        arrow.getPosZ() + MathUtils.random(-0.5f, 0.5f)),
                                new Vector3d(MathUtils.random(-0.5f, 0.5f),
                                        MathUtils.random(-0.3f, 0.3f),
                                        MathUtils.random(-0.5f, 0.5f)).mul(2 * (1 + Math.random())),
                                flameParticles.size(),
                                ColorUtils.random().hashCode()));
                    }
                }
            }
        }


        targetParticles.removeIf(particle -> particle.getTime().isReached(5000));
        flameParticles.removeIf(particle -> particle.getTime().isReached(3500));
    }

    @Subscribe
    public void onChange(EventChangeWorld e) {
        targetParticles.clear();
        flameParticles.clear();
    }

    @Subscribe
    public void onRender(EventPreRender3D event) {
        MatrixStack matrix = event.getMatrix();

        boolean light = GL11.glIsEnabled(GL11.GL_LIGHTING);
        RenderSystem.pushMatrix();
        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        if (light)
            RenderSystem.disableLighting();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        float pos = 0.1F;

        matrix.push();
        if (!targetParticles.isEmpty()) {
            targetParticles.forEach(Particle3D::update);
            for (final Particle3D particle : targetParticles) {
                RectUtils.bindTexture(particle.getTexture());
                if ((int) particle.getAnimation().getValue() != 255 && !particle.getTime().isReached(500)) {
                    particle.getAnimation().run(255);
                }
                if ((int) particle.getAnimation().getValue() != 0 && particle.getTime().isReached(2000)) {
                    particle.getAnimation().run(0);
                }
                int color = ColorUtils.setAlpha(Theme.rectColor, (int) (particle.getAnimation().getValue()));
                if (randomColor.getValue())
                    color = ColorUtils.setAlpha(particle.getColor(), (int) (particle.getAnimation().getValue()));
                final Vector3d v = particle.getPosition();

                final float x = (float) v.x;
                final float y = (float) v.y;
                final float z = (float) v.z;

                matrix.push();
                RectUtils.setupOrientationMatrix(matrix, x, y, z);

                matrix.rotate(mc.getRenderManager().getCameraOrientation());
                matrix.rotate(new Quaternion(new Vector3f(0, 0, 1), particle.rotation, false));
                matrix.push();
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                matrix.translate(0, pos / 2F, 0);
                RectUtils.drawRect(matrix, -pos, -pos, pos, pos, color, color, color, color, true, true);
                float size = pos / 2F;
                color = ColorUtils.setAlpha(-1, (int) (particle.getAnimation().getValue()));
                RectUtils.drawRect(matrix, -size, -size, size, size, color, color, color, color, true, true);

                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                matrix.pop();
                matrix.pop();
            }

        }
        matrix.pop();

        matrix.push();
        if (!flameParticles.isEmpty()) {
            flameParticles.forEach(Particle3D::update);
            for (final Particle3D particle : flameParticles) {
                RectUtils.bindTexture(particle.getTexture());
                if ((int) particle.getAnimation().getValue() != 255 && !particle.getTime().isReached(500)) {
                    particle.getAnimation().run(255);
                }
                if ((int) particle.getAnimation().getValue() != 0 && particle.getTime().isReached(800)) {
                    particle.getAnimation().run(0);
                }
                int color = ColorUtils.setAlpha(Theme.rectColor, (int) (particle.getAnimation().getValue()));
                if (randomColor.getValue())
                    color = ColorUtils.setAlpha(particle.getColor(), (int) (particle.getAnimation().getValue()));

                final Vector3d v = particle.getPosition();

                final float x = (float) v.x;
                final float y = (float) v.y;
                final float z = (float) v.z;

                matrix.push();

                RectUtils.setupOrientationMatrix(matrix, x, y, z);

                matrix.rotate(mc.getRenderManager().getCameraOrientation());
                matrix.rotate(new Quaternion(new Vector3f(0, 0, 1), particle.rotation, false));
                matrix.push();
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                matrix.translate(0, pos / 2F, 0);
                RectUtils.drawRect(matrix, -pos, -pos, pos, pos, color, color, color, color, true, true);
                float size = pos / 2F;
                color = ColorUtils.setAlpha(-1, (int) (particle.getAnimation().getValue()));
                RectUtils.drawRect(matrix, -size, -size, size, size, color, color, color, color, true, true);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                matrix.pop();
                matrix.pop();
            }

        }
        matrix.pop();

        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.clearCurrentColor();
        GL11.glShadeModel(GL11.GL_FLAT);
        if (light)
            RenderSystem.enableLighting();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableAlphaTest();
        matrix.pop();
        RenderSystem.popMatrix();
    }

    @Getter
    public class Particle3D {
        private final int index;
        private final int color;
        private final TimerUtils time = new TimerUtils();
        private final CompactAnimation animation = new CompactAnimation(Easing.LINEAR, 500);
        private ResourceLocation texture;
        public final Vector3d position;
        private final Vector3d delta;

        private float rotate = 0;
        private float rotation;

        public Particle3D(final Vector3d position, final Vector3d velocity, final int index, int color) {
            this.position = position;
            this.delta = new Vector3d(velocity.x * 0.01, velocity.y * 0.01, velocity.z * 0.01);
            this.index = index;
            this.color = color;
            this.texture = texture();
            this.time.reset();
        }

        public void update() {
            rotation = rotate % 1000f / 50f;

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

            this.updateWithoutPhysics();
            rotate += 1;
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

        public void updateWithoutPhysics() {
            this.position.x += this.delta.x;
            this.position.y += this.delta.y;
            this.position.z += this.delta.z;
            this.delta.x /= 0.999999F;
            this.delta.y -= 0.00005F;
            this.delta.z /= 0.999999F;
        }
    }
}
