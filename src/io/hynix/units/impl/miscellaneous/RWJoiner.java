package io.hynix.units.impl.miscellaneous;

import io.hynix.utils.player.InventoryUtils;
import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventPacket;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.johon0.math.TimerUtils;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

@UnitRegister(name = "RWJoiner", category = Category.Miscellaneous, desc = "Автоматически заходит на игровой сервер на ReallyWorld")
public class RWJoiner extends Unit {

    private final SliderSetting griefSelection = new SliderSetting("Номер грифа", 1, 1, 42, 1);
    private final TimerUtils timerUtil = new TimerUtils();
    public RWJoiner() {
        this.addSettings(griefSelection);
    }

    public int grief;

    public void onEnable() {
        int slot = InventoryUtils.getInstance().getSlotInInventoryOrHotbar(Items.COMPASS, true);
        if (slot != -1) {
            mc.player.inventory.currentItem = slot;
            mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
        }
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
        super.onEnable();
    }
    @Subscribe
    private void onUpdate(EventUpdate event) {
        grief = griefSelection.getValue().intValue();
        handleEventUpdate();
    }

    @Subscribe
    public void onPacket(EventPacket eventPacket) {
        String govno;
        if (eventPacket.getPacket() instanceof SJoinGamePacket) {
            try {
                govno = "Вы успешно зашли на " + grief + " гриф!";
                print(govno);
                this.toggle();
            } catch (Exception var5) {
            }
        }

        IPacket packetPacket = eventPacket.getPacket();
        if (packetPacket instanceof SChatPacket packet) {
            govno = TextFormatting.getTextWithoutFormattingCodes(packet.getChatComponent().getString());
            if (govno.contains("К сожалению сервер переполнен") || govno.contains("Подождите 20 секунд!") || govno.contains("большой поток игроков") || govno.contains("Сервер перезагружается")) {
                int slot = InventoryUtils.getInstance().getSlotInInventoryOrHotbar(Items.COMPASS, true);
                if (slot != -1) {
                    mc.player.inventory.currentItem = slot;
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
                }
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            }
        }
    }
    private void handleEventUpdate() {
        if (mc.currentScreen == null) {
            if (mc.player.ticksExisted < 5) {
                if (timerUtil.isReached(100)) {
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    timerUtil.reset();
                }
            }
        } else if (mc.currentScreen instanceof ChestScreen) {
            try {
                int numberGrief = grief;
                ContainerScreen container = (ContainerScreen)mc.currentScreen;

                for(int i = 0; i < container.getContainer().inventorySlots.size(); ++i) {
                    String s = ((Slot)container.getContainer().inventorySlots.get(i)).getStack().getDisplayName().getString();
                    if (ClientUtils.isConnectedToServer("reallyworld") && s.contains("ГРИФЕРСКОЕ ВЫЖИВАНИЕ") && this.timerUtil.isReached(50L)) {
                        mc.playerController.windowClick(mc.player.openContainer.windowId, i, 0, ClickType.PICKUP, mc.player);
                        this.timerUtil.reset();
                    }

                    if (s.contains("ГРИФ #" + numberGrief + " (1.16.5") && this.timerUtil.isReached(50L)) {
                        mc.playerController.windowClick(mc.player.openContainer.windowId, i, 0, ClickType.PICKUP, mc.player);
                        this.timerUtil.reset();
                    }
                }
            } catch (Exception e) {
            }
        }

    }
}
