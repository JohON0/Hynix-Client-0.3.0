package io.hynix.ui.hud.impl;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;

import com.mojang.blaze3d.platform.GlStateManager;
import io.hynix.events.impl.EventRender2D;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.ui.hud.updater.ElementRenderer;
import io.hynix.managers.theme.Theme;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.easing.CompactAnimation;
import io.hynix.utils.johon0.animations.easing.Easing;
import io.hynix.managers.drag.Dragging;
import io.hynix.utils.johon0.animations.impl.EaseBackIn;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.johon0.render.other.Scissor;
import io.hynix.utils.johon0.render.font.Fonts;
import io.hynix.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.potion.Effect;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PotionHud implements ElementRenderer {

    final Dragging dragging;
    private final CompactAnimation widthAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    private final CompactAnimation heightAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    float width;
    float height;
    private Map<String, CompactAnimation> animations = new HashMap<>();

    final AnimationUtils animation = new EaseBackIn(300, 1, 1);

    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack ms = eventRender2D.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 6.5f;
        float padding = 5;
        float iconSize = 10;
        boolean isAnyPotionActive = false;

        for (EffectInstance effectInstance : mc.player.getActivePotionEffects()) {
            if (effectInstance.getDuration() > 0) {
                isAnyPotionActive = true;
                break;
            }
        }

        if (mc.currentScreen instanceof ChatScreen) {
            isAnyPotionActive = true;
        }

        String name = "Potions";
        float nameWidth = Fonts.montserrat.getWidth(name, fontSize, 0.07f);

        animation.setDirection(isAnyPotionActive ? Direction.FORWARDS : Direction.BACKWARDS);
        animation.setDuration(isAnyPotionActive ? 300 : 200);

        GlStateManager.pushMatrix();
        RenderUtils.sizeAnimation(posX + (width / 2), (posY + height / 2), animation.getOutput());

        RenderUtils.drawShadow(posX,posY, (float) width, height, 5, ClickGui.backgroundColor);
        RenderUtils.drawRoundedRect(posX,posY, (float) width, height, 4, ClickGui.backgroundColor);

        float iconOffset = 0;
        for (EffectInstance effectInstance : mc.player.getActivePotionEffects()) {
            if (effectInstance.getDuration() > 0) {
                iconOffset = width-20; // Offset for icons
                break;
            }
        }

        ClientFonts.tenacity[16].drawString(ms, name, posX + iconSize, posY + padding + 4, ClickGui.textcolor);
        ClientFonts.devv[25].drawString(ms, "A", posX + width - padding - 10, posY + 7f, Theme.rectColor);

        posY += fontSize + padding + 2;
        posY += 7f;

        float maxWidth = ClientFonts.tenacityBold[14].getWidth(name) + padding * 2;
        float localHeight = fontSize + padding * 2;

        for (Iterator ef = mc.player.getActivePotionEffects().iterator(); ef.hasNext(); ) {
            EffectInstance effectInstance = (EffectInstance) ef.next();
            String ampStr = "";
            int amp = effectInstance.getAmplifier() + 1;

            if (amp >= 1 && amp <= 9) {
                ampStr = " " + amp;
            }

            String nameText = I18n.format(effectInstance.getEffectName()) + ampStr;
            String durText = EffectUtils.getPotionDurationString(effectInstance, 1.0F);
            float effectWidth = ClientFonts.tenacityBold[14].getWidth(nameText);
            float durWidth = ClientFonts.tenacityBold[14].getWidth(durText);
            float localWidth = effectWidth + durWidth + padding * 3 + 10;

            PotionSpriteUploader potionspriteuploader = mc.getPotionSpriteUploader();
            Effect effect = effectInstance.getPotion();
            TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(effect);

            mc.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());

            CompactAnimation efAnimation = animations.getOrDefault(effectInstance.getEffectName(), null);
            if (efAnimation == null) {
                efAnimation = new CompactAnimation(Easing.EASE_OUT_CIRC, 250);
                animations.put(effectInstance.getEffectName(), efAnimation);
            }

            boolean potionActive = effectInstance.getDuration() > 3;
            efAnimation.run(potionActive ? 1 : 0);

            // Изменяем цвет в зависимости от эффекта
            int color = !effect.isBeneficial() ? Color.RED.getRGB() : ColorUtils.reAlphaInt(ClickGui.textcolor, (int) (255 * efAnimation.getValue()));

            float off = (float) (8 * efAnimation.getValue());

            if (efAnimation.getValue() >= 0.3) {
                float potX = (float) (posX + 3.5f - 1 + 1 * efAnimation.getValue());
                float potY = (float) ((posY + 2.5f - 5 * efAnimation.getValue()));
                DisplayEffectsScreen.blit(ms, potX, potY, 10, off, off, textureatlassprite);
            }

            Scissor.push();
            Scissor.setFromComponentCoordinates(posX, posY - 2, width, height + 2);
            ClientFonts.tenacityBold[14].drawString(ms, nameText, posX + padding + 0.5f + off, (float) (posY + 3.5 - 3 * efAnimation.getValue()), color);
            ClientFonts.tenacityBold[14].drawString(ms, durText, posX + width - padding - durWidth + (8 - off) * 2, (float) (posY + 3.5 - 3 * efAnimation.getValue()), color);
            Scissor.unset();
            Scissor.pop();

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += (float) ((fontSize + padding - 3) * efAnimation.getValue());
            localHeight += (fontSize + padding - 3);
        }

        GlStateManager.popMatrix();

        widthAnimation.run(Math.max(maxWidth, nameWidth + iconSize + 25));
        width = (float) widthAnimation.getValue();
        heightAnimation.run((localHeight + 5.5f));
        height = (float) heightAnimation.getValue();
        dragging.setWidth(width);
        dragging.setHeight(height);
    }
}