package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.EventKey;
import io.hynix.events.impl.EventPacket;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BindSetting;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.InventoryUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.text.TextFormatting;


@FieldDefaults(level = AccessLevel.PRIVATE)
@UnitRegister(name = "ElytraHelper", category = Category.Miscellaneous, desc = "Помощник с Элитрами")
public class ElytraHelper extends Unit {

    final BindSetting swapChestKey = new BindSetting("Кнопка свапа", -1);
    final BindSetting fireWorkKey = new BindSetting("Кнопка феерверков", -1);

    final BooleanSetting autoFireWork = new BooleanSetting("Авто феерверк", true);
    final SliderSetting timerFireWork = new SliderSetting("Таймер феера", 400, 100, 2000, 10).setVisible(() -> autoFireWork.getValue());
    final BooleanSetting autoFly = new BooleanSetting("Авто взлёт", true);
    final InventoryUtils.Hand handUtil = new InventoryUtils.Hand();

    public ElytraHelper() {
        addSettings(swapChestKey, fireWorkKey, autoFly, autoFireWork, timerFireWork);
    }

    // Переменные для управления состоянием
    ItemStack currentStack = ItemStack.EMPTY;
    public static TimerUtils timerUtils = new TimerUtils();
    public static TimerUtils fireWorkTimerUtils = new TimerUtils();
    long delay;

    public TimerUtils wait = new TimerUtils();

    @Subscribe
    private void onEventKey(EventKey e) {
        // Обработка нажатий клавиш
        if (e.getKey() == swapChestKey.getValue() && timerUtils.isReached(700L)) {
            changeChestPlate(currentStack);
            timerUtils.reset();
        }

        if (e.getKey() == fireWorkKey.getValue() && timerUtils.isReached(200L)) {
            if (mc.player.isElytraFlying())
                InventoryUtils.inventorySwapClick(Items.FIREWORK_ROCKET, false); // Использование феерверков
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        currentStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST); // Получение текущего предмета в нагруднике

        if (mc.player != null) {
            final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack,
                    mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump,
                    mc.gameSettings.keyBindSprint};
            if (!wait.isReached(400)) {
                for (KeyBinding keyBinding : pressedKeys) {
                    keyBinding.setPressed(false);
                }
            }
        }

        // Авто взлет
        if (autoFly.getValue() && currentStack.getItem() == Items.ELYTRA) {
            boolean isOnGround = mc.player.isOnGround();
            boolean isElytraFlying = mc.player.isElytraFlying();
            boolean isFlying = mc.player.abilities.isFlying;

            if (isOnGround) {
                mc.player.jump();
            } else if (ElytraItem.isUsable(currentStack) && !isElytraFlying && !isFlying) {
                mc.player.startFallFlying();
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            }
        }

        // Авто использование феерверков
        if (mc.player.isElytraFlying() && autoFireWork.getValue() && !(mc.player.isHandActive() && mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT)) {
            if (fireWorkTimerUtils.isReached(timerFireWork.getValue().longValue())) {
                InventoryUtils.inventorySwapClick(Items.FIREWORK_ROCKET, false);
                fireWorkTimerUtils.reset();
            }
        }

        handUtil.handleItemChange(System.currentTimeMillis() - delay > 200L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        handUtil.onEventPacket(e);
    }

    private void changeChestPlate(ItemStack stack) {
        if (mc.currentScreen != null) {
            return;
        }
        if (stack.getItem() != Items.ELYTRA) {
            int elytraSlot = getItemSlot(Items.ELYTRA);
            if (elytraSlot >= 0) {
                InventoryUtils.moveItem(elytraSlot, 6);
                print(TextFormatting.RED + "Свапнул на элитру!");
                return;
            } else {
                print("Элитра не найдена!");
            }
        }
        int armorSlot = getChestPlateSlot();
        if (armorSlot >= 0) {
            InventoryUtils.moveItem(armorSlot, 6);
            print(TextFormatting.RED + "Свапнул на нагрудник!");
        } else {
            print("Нагрудник не найден!");
        }
    }

    private int getChestPlateSlot() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE};

        for (Item item : items) {
            for (int i = 0; i < 36; ++i) {
                Item stack = mc.player.inventory.getStackInSlot(i).getItem();
                if (stack == item) {
                    if (i < 9) {
                        i += 36;
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        timerUtils.reset();
        super.onDisable();
    }

    private int getItemSlot(Item input) {
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == input) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }
}

