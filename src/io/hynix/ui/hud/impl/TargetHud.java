package io.hynix.ui.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import io.hynix.HynixMain;
import io.hynix.events.impl.AttackEvent;
import io.hynix.events.impl.EventRender2D;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.ui.hud.updater.ElementRenderer;
import io.hynix.managers.theme.Theme;
import io.hynix.units.api.UnitManager;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.impl.EaseBackIn;
import io.hynix.managers.drag.Dragging;
import io.hynix.utils.johon0.math.MathUtils;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.johon0.math.Vector4i;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.johon0.render.other.Scissor;
import io.hynix.utils.johon0.render.other.Stencil;
import io.hynix.utils.johon0.render.render2d.RectUtils;
import io.hynix.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TargetHud implements ElementRenderer {
    final TimerUtils timerUtils = new TimerUtils();
    @Getter
    final Dragging drag;
    @Getter
    LivingEntity entity = null;
    boolean allow;
    final AnimationUtils animation = new EaseBackIn(400, 1, 1);
    float healthAnimation = 0.0f;
    float absorptionAnimation = 0.0f;
    float width = 168 / 1.5f;
    float height = 55 / 1.5f;

    int headSize = 25;
    float spacing = 5;
    private float health;

    public void onAttack(AttackEvent e) {
        if (e.entity == mc.player) {
            return;
        }
        if (entity == null) {
            return;
        }
        if (e.entity instanceof LivingEntity) {
            for (int i = 0; i < 7; ++i) {
                HynixMain.getInstance().getModuleManager().getHud().getParticles().add(new HeadParticle(new Vector3d(drag.getX() + spacing + headSize / 2f, drag.getY() + spacing + headSize / 2f, 0.0)));
            }
        }
    }

    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack ms = eventRender2D.getMatrixStack();
        entity = getTarget(entity);
        float size;
        float health = 0;
        boolean nigggerBotIsState = HynixMain.getInstance().getModuleManager().getTriggerbot().isEnabled();
        boolean hitAuraIsState = HynixMain.getInstance().getModuleManager().getAttackAura().isEnabled();
        boolean out = nigggerBotIsState && !hitAuraIsState ? timerUtils.isReached(500) : !allow; // maybe !allow
        animation.setDuration(out ? 300 : 200);
        animation.setDirection(out ? Direction.BACKWARDS : Direction.FORWARDS);
        FloatFormatter formatter = new FloatFormatter();

        if (animation.getOutput() == 0.0f) {
            entity = null;
        }

        if (entity != null) {
            String name = entity.getName().getString();

            float posX = drag.getX();
            float posY = drag.getY();

            Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));

            float hp = entity.getHealth();
            float maxHp = entity.getMaxHealth();

            if (entity instanceof PlayerEntity) {
                if (HynixMain.getInstance().getModuleManager().getHud().fixhp.getValue()) {
                    hp = score.getScorePoints();
                    maxHp = 20;
                }
            }

            healthAnimation = MathUtils.fast(healthAnimation, MathHelper.clamp(hp / maxHp, 0, 1), 10);
            absorptionAnimation = MathUtils.fast(absorptionAnimation, MathHelper.clamp(entity.getAbsorptionAmount() / maxHp, 0, 1), 10);
            float animationValue = (float) animation.getOutput();
            float halfAnimationValueRest = (1 - animationValue) / 2f;
            float testX = posX + (width * halfAnimationValueRest);
            float testY = posY + (height * halfAnimationValueRest);
            float testW = width * animationValue;
            float testH = height * animationValue;
            float finalHp;

            if (HynixMain.getInstance().getModuleManager().getHud().fixhp.getValue()) {
                finalHp = formatter.format(hp);
            } else {
                finalHp = formatter.format((hp + entity.getAbsorptionAmount()));
            }


            String ownDivider = "";
            String ownStatus = "";

            if (entity != mc.player) {
                ownDivider = " / ";
                double targetCheck = MathUtils.entity(entity, true, true, false, 1, false);
                double playerCheck = MathUtils.entity(mc.player, true, true, false,1, false);
                if (targetCheck > playerCheck) {
                    ownStatus = "Lose";
                } else if (targetCheck < playerCheck) {
                    ownStatus = "Win";
                } else if (targetCheck == playerCheck) {
                    ownStatus = "Tie";
                } else {
                    ownStatus = "Unknown";
                }
            } else {
                ownStatus = ownDivider = "";
            }
            GlStateManager.pushMatrix();
            RenderUtils.sizeAnimation(posX + (width / 2), posY + height / 2, animation.getOutput());
                height = 50 / 1.5f;
                width = 160 / 1.5f;
                headSize = 25;
            RenderUtils.drawShadow(posX,posY, (float) width, height, 5, ClickGui.backgroundColor);
            RenderUtils.drawRoundedRect(posX,posY, (float) width, height, 4, ClickGui.backgroundColor);
            Scissor.push();
            Scissor.setFromComponentCoordinates(posX, posY, 100, height);
            //skin
            String rs = EntityType.getKey(((Entity)entity).getType()).getPath();
            ResourceLocation skin = entity instanceof AbstractClientPlayerEntity e ? (e).getLocationSkin() : new ResourceLocation("textures/entity/"+rs+".png");
            try{
                mc.getResourceManager().getResource(skin);
            }catch(Exception e){
                if(!(entity instanceof AbstractClientPlayerEntity)){skin = new ResourceLocation("textures/entity/"+rs+"/"+rs+".png");}
            }
            GL11.glEnable(GL11.GL_BLEND);

            GL11.glPushMatrix();
            float hurtPercent = RenderUtils.getHurtPercent(entity)/2;
            GL11.glColor4f(1, 0.1f - hurtPercent, 0.1f - hurtPercent, 1);
            GL11.glColor4f(1, 0.1f - hurtPercent, 0.1f - hurtPercent, 1);

            RenderUtils.drawHead(skin,posX+1.5f,posY+0.5f,28,28,3,1,hurtPercent);


            GL11.glColor4f(1, 1, 1, 1);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
            Scissor.unset();
            Scissor.pop();

                Scissor.push();
                Scissor.setFromComponentCoordinates(testX, testY, testW, testH);
                ClientFonts.tenacity[13].drawString(eventRender2D.getMatrixStack(), name, posX - 0.5f + headSize + spacing + spacing, posY + spacing + 3, ClickGui.textcolor);
                ClientFonts.tenacity[13].drawString(eventRender2D.getMatrixStack(), "Health: " + (finalHp) + ownDivider + ownStatus, posX - 0.5f + headSize + spacing + spacing, posY + spacing + spacing + spacing + 2, ClickGui.textcolor);
                Scissor.unset();
                Scissor.pop();

                Vector4i vector4i = new Vector4i(Theme.rectColor, Theme.rectColor, Theme.mainRectColor, Theme.mainRectColor);

                RenderUtils.drawRoundedRect(posX + 5 + headSize + spacing + spacing - 5, posY + height - spacing * 2, (width - 40), 5, new Vector4f(3, 3, 3, 3), ClickGui.lightcolor);
                RenderUtils.drawRoundedRect(posX + 5 + headSize + spacing + spacing - 5, posY + height - spacing * 2, (width - 40) * healthAnimation, 5, new Vector4f(3, 3, 3, 3), vector4i);
                RenderUtils.drawShadow(posX + 5 + headSize + spacing + spacing - 5, posY + height - spacing * 2, (width - 40) * healthAnimation, 5, 8, ColorUtils.setAlpha(Theme.rectColor, 80), ColorUtils.setAlpha(Theme.rectColor, 80));

            GlStateManager.popMatrix();
        }

        if (HynixMain.getInstance().getModuleManager().getHud().particlesOnTarget.getValue()) {
            for (HeadParticle p : HynixMain.getInstance().getModuleManager().getHud().getParticles()) {
                if (System.currentTimeMillis() - p.time > 2000L) {
                    HynixMain.getInstance().getModuleManager().getHud().getParticles().remove(p);
                }
                p.update();
                size = 1.0f - (float)(System.currentTimeMillis() - p.time) / 2000.0f;
                RenderUtils.drawCircle((float)p.pos.x, (float)p.pos.y, 3.5f, ColorUtils.setAlpha(Theme.rectColor, (int)(255.0f * p.alpha * size)));
            }
        }
        drag.setWidth(width);
        drag.setHeight(height);

    }

    private LivingEntity getTarget(LivingEntity nullTarget) {
        boolean hitAuraIsState = HynixMain.getInstance().getModuleManager().getAttackAura().isEnabled();
        LivingEntity niggerBotTarget = HynixMain.getInstance().getModuleManager().getTriggerbot().getTarget();
        LivingEntity finalTarget = hitAuraIsState ? HynixMain.getInstance().getModuleManager().getAttackAura().target : niggerBotTarget;
        LivingEntity target = nullTarget;
        if (finalTarget != null) {
            timerUtils.reset();
            allow = true;
            target = finalTarget;
        } else if (mc.currentScreen instanceof ChatScreen) {
            timerUtils.reset();
            allow = true;
            target = mc.player;
        } else {
            allow = false;
        }
        return target;
    }

    private void drawHead(MatrixStack matrix, final Entity entity, final double x, final double y, final int size) {
        if (entity instanceof AbstractClientPlayerEntity player) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0);
            RenderSystem.enableTexture();
            mc.getTextureManager().bindTexture(player.getLocationSkin());
            // Проверка на наличие текстуры
            if (player.getLocationSkin() == null) {
                // Вместо этого просто возврат или создание текстуры по умолчанию.
                return;
            }
            float hurtPercent = (((AbstractClientPlayerEntity) entity).hurtTime - (((AbstractClientPlayerEntity) entity).hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f)) / 10.0f;
            RenderSystem.color4f(1, 1 - hurtPercent, 1 - hurtPercent, 1);
            AbstractGui.blit(matrix, (float) x, (float) y, size, size, 4F, 4F, 4F, 4F, 32F, 32F);
            RenderUtils.scaleStart((float) (x + size / 2F), (float) (y + size / 2F), 1.1F);
            AbstractGui.blit(matrix, (float) x, (float) y, size, size, 20, 4, 4, 4, 32, 32);
            RenderUtils.scaleEnd();
            RenderSystem.disableBlend();
        } else {
            int color = ColorUtils.getColor(20, 128);
            RectUtils.getInstance().drawRoundedRectShadowed(matrix, (float) x, (float) y, (float) (x + size), (float) (y + size), 2F, 1, color, color, color, color, false, false, true, true);
            ClientFonts.interRegular[size * 2].drawCenteredString(matrix, "?", x + (size / 2F), y + 3 + (size / 2F) - (ClientFonts.interRegular[size * 2].getFontHeight() / 2F), -1);
        }
    }

    private void drawItemStack(float x, float y, float offset, float scale) {
        ArrayList<ItemStack> stackList = new ArrayList(Arrays.asList(this.entity.getHeldItemMainhand(), this.entity.getHeldItemOffhand()));
        stackList.addAll((Collection)this.entity.getArmorInventoryList());
        AtomicReference<Float> posX = new AtomicReference(x);
        stackList.stream().filter((stack) -> {
            return !stack.isEmpty();
        }).forEach((stack) -> {
            drawItemStack(stack, (Float)posX.getAndAccumulate(offset, Float::sum), y, true, true, scale);
        });
    }
    private float fix1000Health(Entity entity) {
        Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(),
                mc.world.getScoreboard().getObjectiveInDisplaySlot(2));

        LivingEntity living = (LivingEntity) entity;

        return userConnectedToFunTimeAndEntityIsPlayer(entity) ? score.getScorePoints() : MathHelper.clamp(living.getHealth(), 0, 20);
    }
    private boolean userConnectedToFunTimeAndEntityIsPlayer(Entity entity) {
        UnitManager unitManager = HynixMain.getInstance().getModuleManager();
        String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();
        return (mc.getCurrentServerData() != null && unitManager.getHud().fixhp.getValue() && entity instanceof PlayerEntity);
    }

    public void drawItemStack(ItemStack stack, float x, float y, boolean withoutOverlay, boolean scale, float scaleValue) {
        RenderSystem renderSystem = new RenderSystem();
        renderSystem.pushMatrix();
        renderSystem.translatef(x, y, 0.0F);
        if (scale) {
            GL11.glScaled((double)scaleValue, (double)scaleValue, (double)scaleValue);
        }

        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
        if (withoutOverlay) {
            mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, stack, 0, 0);
        }

        renderSystem.popMatrix();
    }

    public class FloatFormatter {
        public float format(float value) {
            float multiplier = (float) Math.pow(10, 1);
            return Math.round(value * multiplier) / multiplier;
        }
    }

    public static class HeadParticle {
        private Vector3d pos;
        private final Vector3d end;
        private final long time;
        private float alpha;

        public HeadParticle(Vector3d pos) {
            this.pos = pos;
            this.end = pos.add((double)(-ThreadLocalRandom.current().nextFloat(-80.0F, 80.0F)), (double)(-ThreadLocalRandom.current().nextFloat(-80.0F, 80.0F)), (double)(-ThreadLocalRandom.current().nextFloat(-80.0F, 80.0F)));
            this.time = System.currentTimeMillis();
        }

        public void update() {
            this.alpha = MathUtils.lerp(this.alpha, 1.0F, 10.0F);
            this.pos = MathUtils.fast(this.pos, this.end, 0.5F);
        }
    }
}
