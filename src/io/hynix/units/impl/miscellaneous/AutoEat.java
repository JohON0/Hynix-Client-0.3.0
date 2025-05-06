package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;
import io.hynix.HynixMain;
import io.hynix.events.impl.EventUpdate;
import io.hynix.managers.premium.PremiumChecker;
import io.hynix.ui.notifications.impl.WarningNotify;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

/**
 * @author JohON0
 * @date 06.01.24
 */
@UnitRegister(name = "AutoEat", category = Category.Miscellaneous, desc = "Автоматически кушает", premium = true)
public class AutoEat extends Unit {
    boolean isEating = false;

    public int findEatSlot() {
        ItemStack offHandStack = mc.player.getHeldItemOffhand();
        if (offHandStack.getUseAction() == UseAction.EAT) {
            return 40;
        }

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(slot);
            if (stack.getUseAction() == UseAction.EAT) {
                return slot;
            }
        }
        return -1;
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        int slot = findEatSlot();
        if (slot != -1) {
            ItemStack currentItem = mc.player.inventory.getStackInSlot(slot);
            int foodLevel = mc.player.getFoodStats().getFoodLevel();

            if (foodLevel >= 20) {
                isEating = false;
                mc.gameSettings.keyBindUseItem.pressed = false;
                return;
            }

            if ((currentItem.getItem() == Items.COOKED_BEEF && foodLevel <= 12) ||
                    (currentItem.getItem() == Items.COOKED_PORKCHOP && foodLevel <= 12) ||
                    (currentItem.getItem() == Items.COOKED_RABBIT && foodLevel <= 14) ||
                    (currentItem.getItem() == Items.COOKED_CHICKEN && foodLevel <= 14) ||
                    (currentItem.getItem() == Items.COOKED_COD && foodLevel <= 14) ||
                    (currentItem.getItem() == Items.COOKED_MUTTON && foodLevel <= 14) ||
                    (currentItem.getItem() == Items.BREAD && foodLevel <= 14)) {

                mc.player.inventory.currentItem = slot;
                mc.gameSettings.keyBindUseItem.pressed = true;

                isEating = true;
            } else {
                isEating = false;
                mc.gameSettings.keyBindUseItem.pressed = false;
            }
        } else {
            isEating = false;
            mc.gameSettings.keyBindUseItem.pressed = false;
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (!PremiumChecker.isPremium) {
            toggle();
            HynixMain.getInstance().getNotifyManager().add(new WarningNotify("Модуль " + getName() + " работает только для " + TextFormatting.GOLD + "премиум " + TextFormatting.WHITE + "пользователей!", 5000));
            print("Предупреждение: Модуль " + getName() + " работает только для премиум пользователей! Если хочешь подержать проект, то премиум-подписку можно преобрести на сайте https://hynix.fun/");
        }
    }
    @Override
    public void onDisable() {
        super.onDisable();
        isEating = false;
        mc.gameSettings.keyBindUseItem.pressed = false; // Отключаем нажатие кнопки при отключении
    }
}
