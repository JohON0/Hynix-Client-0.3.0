package io.hynix.units.impl.combat;

import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.MoveUtils;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
/**
 * @author JohON0
 */
@UnitRegister(
        name = "AutoArmor",
        category = Category.Combat,
        desc = "Сам одевает броню"
)
public class AutoArmor extends Unit {

    final SliderSetting delay = new SliderSetting("Задержка", 100.0f, 0.0f, 1000.0f, 1.0f);
    final BooleanSetting onlyInv = new BooleanSetting("Только в инве", false);
    final BooleanSetting workInMove = new BooleanSetting("Работать в движении", true);
    final TimerUtils timerUtils = new TimerUtils();


    public void onUpdate(EventUpdate e) {
        if (!workInMove.getValue()) {
            if (MoveUtils.isMoving()) {
                return;
            }
        }

        if (onlyInv.getValue()) {
            if (!(mc.currentScreen instanceof InventoryScreen)) {
                return;
            }
        }
        PlayerInventory inventoryPlayer = mc.player.inventory;
        int[] bestIndexes = new int[4];
        int[] bestValues = new int[4];

        for (int i = 0; i < 4; ++i) {
            bestIndexes[i] = -1;
            ItemStack stack = inventoryPlayer.armorItemInSlot(i);

            if (!isItemValid(stack) || !(stack.getItem() instanceof ArmorItem armorItem)) {
                continue;
            }

            bestValues[i] = calculateArmorValue(armorItem, stack);
        }

        for (int i = 0; i < 36; ++i) {
            Item item;
            ItemStack stack = inventoryPlayer.getStackInSlot(i);

            if (!isItemValid(stack) || !((item = stack.getItem()) instanceof ArmorItem)) continue;

            ArmorItem armorItem = (ArmorItem) item;
            int armorTypeIndex = armorItem.getSlot().getIndex();
            int value = calculateArmorValue(armorItem, stack);

            if (value <= bestValues[armorTypeIndex]) continue;

            bestIndexes[armorTypeIndex] = i;
            bestValues[armorTypeIndex] = value;
        }

        ArrayList<Integer> randomIndexes = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(randomIndexes);

        for (int index : randomIndexes) {
            int bestIndex = bestIndexes[index];

            if (bestIndex == -1 || (isItemValid(inventoryPlayer.armorItemInSlot(index)) && inventoryPlayer.getFirstEmptyStack() == -1))
                continue;

            if (bestIndex < 9) {
                bestIndex += 36;
            }

            if (!this.timerUtils.isReached(this.delay.getValue().longValue())) break;

            ItemStack armorItemStack = inventoryPlayer.armorItemInSlot(index);

            if (isItemValid(armorItemStack)) {
                mc.playerController.windowClick(0, 8 - index, 0, ClickType.QUICK_MOVE, mc.player);
            }

            mc.playerController.windowClick(0, bestIndex, 0, ClickType.QUICK_MOVE, mc.player);
            this.timerUtils.reset();
            break;
        }
    }

    private boolean isItemValid(ItemStack stack) {
        return stack != null && !stack.isEmpty();
    }

    public static int calculateArmorValue(final ArmorItem armor, final ItemStack stack) {
        final int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack);
        final IArmorMaterial armorMaterial = armor.getArmorMaterial();
        final int damageReductionAmount = armorMaterial.getDamageReductionAmount(armor.getEquipmentSlot());
        return ((armor.getDamageReduceAmount() * 20 + protectionLevel * 12 + (int) (armor.getToughness() * 2) + damageReductionAmount * 5) >> 3);
    }

    }
