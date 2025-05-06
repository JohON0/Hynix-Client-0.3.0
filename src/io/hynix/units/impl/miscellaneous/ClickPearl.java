package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;

import io.hynix.HynixMain;
import io.hynix.events.impl.EventKey;
import io.hynix.events.impl.EventPacket;
import io.hynix.events.impl.EventTick;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BindSetting;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.InventoryUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
/**
 * @author JohON0
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@UnitRegister(name = "ClickPearl", category = Category.Miscellaneous, desc = "Кидает Эндер-жемчуг на СКМ")
public class ClickPearl extends Unit {
    final ModeSetting mode = new ModeSetting("Тип", "Обычный", "Обычный", "Легитный");
    final BindSetting pearlKey = new BindSetting("Кнопка", -98);
    final InventoryUtils.Hand handUtil = new InventoryUtils.Hand();
    final ItemCooldown itemCooldown;
    long delay;
    final TimerUtils waitMe = new TimerUtils();
    final TimerUtils timerUtils = new TimerUtils();
    final TimerUtils timerUtils2 = new TimerUtils();
    public ActionType actionType = ActionType.START;
    Runnable runnableAction;
    int oldSlot = -1;

    public ClickPearl(ItemCooldown itemCooldown) {
        this.itemCooldown = itemCooldown;
        addSettings(mode, pearlKey);
    }

    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == pearlKey.getValue()) {
            if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL)) {
                final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSprint};
                if (ClientUtils.isConnectedToServer("funtime")) {
                    if (!waitMe.isReached(400)) {
                        for (KeyBinding keyBinding : pressedKeys) {
                            keyBinding.setPressed(false);
                        }
                        return;
                    }
                }

                sendRotatePacket();

                oldSlot = mc.player.inventory.currentItem;

                if (mode.is("Обычный")) {
                    InventoryUtils.inventorySwapClick(Items.ENDER_PEARL, true);
                } else {
                    if (runnableAction == null) {
                        actionType = ActionType.START;
                        runnableAction = () -> vebatSoli();
                        timerUtils.reset();
                        timerUtils2.reset();
                    }
                }
            } else {
                ItemCooldown.ItemEnum itemEnum = ItemCooldown.ItemEnum.getItemEnum(Items.ENDER_PEARL);

                if (itemCooldown.isEnabled() && itemEnum != null && itemCooldown.isCurrentItem(itemEnum)) {
                    itemCooldown.lastUseItemTime.put(itemEnum.getItem(), System.currentTimeMillis());
                }
            }
        }
    }

    @Subscribe
    public void onTick(EventTick e) {
        if (runnableAction != null) {
            runnableAction.run();
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private void vebatSoli() {
        int slot = InventoryUtils.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, true);
        Hand hand = mc.player.getHeldItemOffhand().getItem() instanceof EnderPearlItem ? Hand.OFF_HAND : Hand.MAIN_HAND;

        if (slot != -1) {
            interact(slot, hand);
        } else {
            runnableAction = null;
        }
    }

    private void swingAndSendPacket(Hand hand) {
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(hand));
        mc.player.swingArm(hand);
    }

    private void interact(Integer slot, Hand hand) {
        if (actionType == ActionType.START) { // начало
            switchSlot(slot, hand);
            actionType = ActionType.WAIT;
        } else if (actionType == ActionType.WAIT && timerUtils.isReached(50L)) { // какая та хуйня
            actionType = ActionType.USE_ITEM;
        } else if (actionType == ActionType.USE_ITEM) {
            sendRotatePacket();
            swingAndSendPacket(hand);
            switchSlot(mc.player.inventory.currentItem, hand);
            actionType = ActionType.SWAP_BACK;
        } else if (actionType == ActionType.SWAP_BACK && timerUtils2.isReached(300L)) { // задержка на свап обратно
            mc.player.inventory.currentItem = oldSlot;
            runnableAction = null;
        }
    }

    private void switchSlot(int slot, Hand hand) {
        if (slot != mc.player.inventory.currentItem && hand != Hand.OFF_HAND) {
            mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
            mc.player.inventory.currentItem = slot;
        }
    }

    private void sendRotatePacket() {
        if (HynixMain.getInstance().getModuleManager().getAttackAura().target != null) {
            mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
        }
    }

    public enum ActionType {
        START, WAIT, USE_ITEM, SWAP_BACK
    }
}
