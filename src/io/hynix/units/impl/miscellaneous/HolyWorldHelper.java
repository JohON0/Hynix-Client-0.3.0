package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventKey;
import io.hynix.events.impl.EventMotion;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BindSetting;
import io.hynix.utils.player.InventoryUtils;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

@UnitRegister(name="HolyWorld Helper", category = Category.Miscellaneous, desc="Помощник на сервере HolyWorld")
public class HolyWorldHelper extends Unit {
    private final BindSetting throwKey = new BindSetting("Взрывная Трапка", -1);
    private final BindSetting secondaryKey = new BindSetting("Станка", -1);
    private final BindSetting snowballKey = new BindSetting("Ком Снега", -1);
    private final BindSetting fireworkStarKey = new BindSetting("Прощальный гул", -1);
    private long delay;
    private boolean actionPending;
    private long lastItemThrowTime = 0L;
    private int originalSlot = -1;
    private int itemSlot = -1;
    private Item currentItem;

    public HolyWorldHelper() {
        this.addSettings(this.throwKey, this.secondaryKey, this.snowballKey, this.fireworkStarKey);
    }

    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == (Integer) this.throwKey.getValue()) {
            this.currentItem = Items.PRISMARINE_SHARD;
            this.actionPending = true;
        } else if (e.getKey() == (Integer) this.secondaryKey.getValue()) {
            this.currentItem = Items.NETHER_STAR;
            this.actionPending = true;
        } else if (e.getKey() == (Integer) this.snowballKey.getValue()) {
            this.currentItem = Items.SNOWBALL;
            this.actionPending = true;
        } else if (e.getKey() == (Integer) this.fireworkStarKey.getValue()) {
            this.currentItem = Items.FIREWORK_STAR;
            this.actionPending = true;
        }

    }

    @Subscribe
    private void onMotion(EventMotion e) {
        if (this.actionPending && System.currentTimeMillis() - this.lastItemThrowTime >= 250L) {
            if (this.currentItem != null) {
                if (!mc.player.getCooldownTracker().hasCooldown(this.currentItem)) {
                    this.itemSlot = InventoryUtils.getInstance().getSlotInInventoryOrHotbar(this.currentItem, true);
                    if (this.itemSlot != -1) {
                        this.performThrow(this.itemSlot);
                    }
                }
            }

            this.lastItemThrowTime = System.currentTimeMillis();
            this.actionPending = false;
        }

    }

    private void performThrow(int itemSlot) {
        this.originalSlot = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = itemSlot;
        mc.player.connection.sendPacket(new CHeldItemChangePacket(itemSlot));
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
        mc.player.swingArm(Hand.MAIN_HAND);
        this.delay = System.currentTimeMillis() + 50L;
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (this.delay > 0L && System.currentTimeMillis() >= this.delay) {
            mc.player.inventory.currentItem = this.originalSlot;
            mc.player.connection.sendPacket(new CHeldItemChangePacket(this.originalSlot));
            this.delay = -1L;
        }

    }
}
