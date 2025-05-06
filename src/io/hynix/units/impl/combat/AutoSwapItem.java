package io.hynix.units.impl.combat;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.EventCooldown;
import io.hynix.events.impl.EventKey;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BindSetting;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.InventoryUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.*;
/**
 * @author JohON0
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@UnitRegister(name = "AutoSwapItem", category = Category.Combat,
desc = "Автоматически свапает предметы (указанные в настройках модуля)")
public class AutoSwapItem extends Unit {
    final ModeSetting itemType = new ModeSetting("Предмет", "Щит", "Щит", "Геплы", "Тотем", "Шар");
    final ModeSetting swapType = new ModeSetting("Свапать на", "Геплы", "Щит", "Геплы", "Тотем", "Шар");
    final BindSetting keyToSwap = new BindSetting("Кнопка свапа", 0);
    public TimerUtils stopWatch = new TimerUtils();

    public AutoSwapItem() {
        addSettings(itemType,swapType,keyToSwap);
    }

    @Subscribe
    public void onEventKey(EventKey e) {
        ItemStack offhandItemStack = mc.player.getHeldItemOffhand();
        boolean isOffhandNotEmpty = !(offhandItemStack.getItem() instanceof AirItem);

        if (e.isKeyDown(keyToSwap.getValue()) && stopWatch.isReached(200)) {
            Item currentItem = offhandItemStack.getItem();
            boolean isHoldingSwapItem = currentItem == getSwapItem();
            boolean isHoldingSelectedItem = currentItem == getSelectedItem();
            int selectedItemSlot = getSlot(getSelectedItem());
            int swapItemSlot = getSlot(getSwapItem());

            if (selectedItemSlot >= 0) {
                if (!isHoldingSelectedItem) {
                    InventoryUtils.moveItem(selectedItemSlot, 45, isOffhandNotEmpty);
                    stopWatch.reset();
                    return;
                }
            }
            if (swapItemSlot >= 0) {
                if (!isHoldingSwapItem) {
                    InventoryUtils.moveItem(swapItemSlot, 45, isOffhandNotEmpty);
                    stopWatch.reset();
                }
            }
        }
    }
    private Item getSwapItem() {
        return getItemByType(swapType.getValue());
    }

    private Item getSelectedItem() {
        return getItemByType(itemType.getValue());
    }
    private int getSlot(Item item) {
        int finalSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                if (mc.player.inventory.getStackInSlot(i).isEnchanted()) {
                    finalSlot = i;
                    break;
                } else {
                    finalSlot = i;
                }
            }
        }
        if (finalSlot < 9 && finalSlot != -1) {
            finalSlot = finalSlot + 36;
        }
        return finalSlot;
    }

    private Item getItemByType(String itemType) {
        return switch (itemType) {
            case "Щит" -> Items.SHIELD;
            case "Тотем" -> Items.TOTEM_OF_UNDYING;
            case "Геплы" -> Items.GOLDEN_APPLE;
            case "Шар" -> Items.PLAYER_HEAD;
            default -> Items.AIR;
        };
    }
}