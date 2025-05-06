package io.hynix.ui.hud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.hynix.events.impl.EventRender2D;
import io.hynix.events.impl.EventRender3D;
import io.hynix.managers.drag.Dragging;
import io.hynix.managers.theme.Theme;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.ui.hud.updater.ElementRenderer;
import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.easing.CompactAnimation;
import io.hynix.utils.johon0.animations.easing.Easing;
import io.hynix.utils.johon0.animations.impl.EaseBackIn;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.other.Scissor;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Cooldowns implements ElementRenderer {
    private final Dragging dragging;
    private float width;
    private float height;
    private final CompactAnimation widthAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    private final CompactAnimation heightAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    final AnimationUtils animation = new EaseBackIn(300, 1, 1);

    @Override
    public void render(EventRender2D eventRender2D) {
        MatrixStack ms = eventRender2D.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();
        float fontSize = 6.5f;
        float padding = 5;
        float iconSize = 10;
        String title = "Cooldowns";
        float titleWidth = ClientFonts.tenacity[16].getWidth(title);
        boolean isAnyCooldownActive = false;

        // Проверяем активные кулдауны
        Item[] itemsToCheck = new Item[]{
                Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE,
                Items.ENDER_PEARL, Items.CHORUS_FRUIT,
                Items.POTION, Items.ENDER_EYE,
                Items.SUGAR, Items.NETHERITE_SCRAP
        };

        for (Item item : itemsToCheck) {
            float cooldown = Minecraft.getInstance().player.getCooldownTracker().getCooldown1(item, 0.0F);
            if (cooldown > 0.1F || mc.currentScreen instanceof ChatScreen) {
                isAnyCooldownActive = true;
                break;
            }
        }

        animation.setDirection(isAnyCooldownActive ? Direction.FORWARDS : Direction.BACKWARDS);
        animation.setDuration(isAnyCooldownActive ? 300 : 200);

        GlStateManager.pushMatrix();
        RenderUtils.sizeAnimation(posX + (width / 2), (posY + height / 2), animation.getOutput());
        RenderUtils.drawShadow(posX, posY, width, height, 5, ClickGui.backgroundColor);
        RenderUtils.drawRoundedRect(posX, posY, width, height, 4, ClickGui.backgroundColor);
        //чек названия предметов
        // Заголовок
        ClientFonts.tenacity[16].drawString(ms, title, posX + iconSize, posY + padding + 4, ClickGui.textcolor);
        ClientFonts.icons_nur[19].drawString(ms, "O", posX + width - padding - 10, posY + 8f, Theme.rectColor);

        float maxWidth = titleWidth + padding * 2;
        float localHeight = fontSize + padding * 2;
        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);
        posY += fontSize + padding + 2f;
        posY += 7f;

        for (Item item : itemsToCheck) {

            long cooldown = Minecraft.getInstance().player.getCooldownTracker().getCooldown1(item, 0.0F);
            if (cooldown <= 0.1F) continue;

            // Создаем ItemStack для получения правильного названия
            ItemStack itemStack = new ItemStack(item);
            String itemName = itemStack.getDisplayName().getString(); // Имя предмета из игры с учётом переименования
            String cooldownText = formatCooldown(cooldown);
            int color = ClickGui.textcolor;
            String nameitem = itemName;
            if (ClientUtils.isConnectedToServer("funsky") || ClientUtils.isConnectedToServer("funtime")) {
                if (item.equals(Items.ENCHANTED_GOLDEN_APPLE)) {
                    nameitem = "Зачарованное яблоко";
                } else if (item.equals(Items.GOLDEN_APPLE)) {
                    nameitem = "Золотое яблоко";
                } else if (item.equals(Items.ENDER_PEARL)) {
                    nameitem = "Эндер жемчуг";
                } else if (item.equals(Items.CHORUS_FRUIT)) {
                    nameitem = "Хорус";
                } else if (item.equals(Items.POTION)) {
                    nameitem = "Зелье";
                } else if (item.equals(Items.ENDER_EYE)) {
                    nameitem = "Дезорентация";
                } else if (item.equals(Items.SUGAR)) {
                    nameitem = "Явная пыль";
                } else if (item.equals(Items.NETHERITE_SCRAP)) {
                    nameitem = "Трапка";
                } else {
                    nameitem = "Неизвестный предмет"; // На всякий случай для других предметов.
                }
            }

            float itemWidth = ClientFonts.tenacityBold[14].getWidth(nameitem) + 10;
            float cooldownWidth = ClientFonts.tenacityBold[14].getWidth(cooldownText);
            float localWidth = itemWidth + cooldownWidth + padding * 3;

            ClientFonts.tenacityBold[14].drawString(ms, nameitem,
                    posX + padding, posY + 1.5f, color);
            ClientFonts.tenacityBold[14].drawString(ms, cooldownText,
                    posX + width - padding - cooldownWidth, posY + 1.5f, color);

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }

            posY += fontSize + padding - 3f;
            localHeight += fontSize + padding - 3f;
        }



        Scissor.unset();
        Scissor.pop();

        GlStateManager.popMatrix();

        widthAnimation.run(Math.max(maxWidth, titleWidth + iconSize + 25));
        width = (float) widthAnimation.getValue();
        heightAnimation.run(localHeight + 5.5);
        height = (float) heightAnimation.getValue();

        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    private String formatCooldown(long millis) {
        if (millis <= 0) {
            return "0 сек";
        }

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return String.format("%d мин %d сек", minutes, seconds);
        } else {
            return String.format("%d сек", seconds);
        }
    }
}