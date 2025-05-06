package io.hynix.ui.hud.impl;

import io.hynix.HynixMain;
import io.hynix.events.impl.EventRender2D;
import io.hynix.units.impl.display.Crosshair;
import io.hynix.ui.hud.updater.ElementRenderer;
import io.hynix.utils.johon0.math.MathUtils;

import io.hynix.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

import java.util.Iterator;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ArmorHud implements ElementRenderer {

    private static final ResourceLocation TOTEM_TEXTURE = new ResourceLocation("minecraft", "textures/item/totem_of_undying.png");
    float animation;
    @Override
    public void render(EventRender2D eventRender2D) {
        animation = MathUtils.lerp(animation, mc.currentScreen instanceof ChatScreen ? window.getScaledHeight() - 33 : window.getScaledHeight() - (16 + 2), 15);
        int posX = (int) (scaled().x / 2 + 95);
        int posY = (int) animation;
        float addX = 0, addY = 0;

        Crosshair crosshair = HynixMain.getInstance().getModuleManager().getCrosshair();
        if (crosshair.isEnabled() && crosshair.mode.is("Орбиз") && !crosshair.staticCrosshair.getValue() && mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
            addX = crosshair.getAnimatedYaw();
            addY = crosshair.getAnimatedPitch();
        }

        float size = 14;
        float totemX = window.getScaledWidth() / 2f - size / 2f + addX;
        float totemY = window.getScaledHeight() / 2f + 7 + addY;

        int countItems = this.countItems(Items.TOTEM_OF_UNDYING);
        countItems += this.countItemsInOffhand(Items.TOTEM_OF_UNDYING);

        String text = String.valueOf(countItems);

        if (countItems > 99) {
            text = "99+";
        }

        if (countItems > 0) {
            mc.getTextureManager().bindTexture(TOTEM_TEXTURE);
            AbstractGui.blit(eventRender2D.getMatrixStack(), totemX, totemY, 0.0F, 0.0F, size, size, size, size);;
            ClientFonts.tenacityBold[16].drawCenteredStringWithOutline(eventRender2D.getMatrixStack(), text, totemX + size / 2f, totemY + size - 1, -1);
        }

        for (ItemStack itemStack : mc.player.getArmorInventoryList()) {
            if (itemStack.isEmpty()) continue;

            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, posX, posY);
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, posX, posY, null);

            posX += 16 + 2;
        }
    }

    private int countItems(Item item) {
        int i = 0;
        Iterator iterator = mc.player.inventory.mainInventory.iterator();

        while(iterator.hasNext()) {
            ItemStack itemStack = (ItemStack)iterator.next();
            if (itemStack.getItem() == item) {
                i += itemStack.getCount();
            }
        }

        return i;
    }

    private int countItemsInOffhand(Item item) {
        ItemStack offhand = mc.player.getHeldItemOffhand();
        return offhand.getItem() == item ? offhand.getCount() : 0;
    }
}
