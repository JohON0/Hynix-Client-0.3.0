package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventInventoryClose;
import io.hynix.events.impl.EventPacket;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.MoveUtils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.AnvilScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;

import java.util.ArrayList;
import java.util.List;

@UnitRegister(name = "InventoryMove", category = Category.Miscellaneous,desc = "Позволяет ходить с открытым инвентарем")
public class InventoryMove extends Unit {
    private final List<IPacket<?>> packet = new ArrayList<>();
    public TimerUtils timerUtils = new TimerUtils();

    private void updateKeyBindingState(KeyBinding[] keyBindings) {
        for (KeyBinding keyBinding : keyBindings) {
            boolean isKeyPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }
    @Subscribe
    public void onPacket(EventPacket e) {
        if (ClientUtils.isConnectedToServer("funtime")) {
            if (e.getPacket() instanceof CClickWindowPacket p && MoveUtils.isMoving()) {
                if (mc.currentScreen instanceof InventoryScreen) {
                    packet.add(p);
                    e.cancel();
                }
            }
        }
    }
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player != null) {
            final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSprint};
            if (ClientUtils.isConnectedToServer("funtime")) {
                if (!timerUtils.isReached(400)) {
                    for (KeyBinding keyBinding : pressedKeys) {
                        keyBinding.setPressed(false);
                    }
                    return;
                }
            }


            if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen || mc.currentScreen instanceof AnvilScreen) {
                return;
            }

            updateKeyBindingState(pressedKeys);
        }
    }
    @Subscribe
    public void onClose(EventInventoryClose e) {
        if (ClientUtils.isConnectedToServer("funtime")) {
            if (mc.currentScreen instanceof InventoryScreen && !packet.isEmpty() && MoveUtils.isMoving()) {
                new Thread(() -> {
                    timerUtils.reset();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    for (IPacket p : packet) {
                        mc.player.connection.sendPacketWithoutEvent(p);
                    }
                    packet.clear();
                }).start();
                e.cancel();
            }
        }
    }
}
