package io.hynix.units.impl.traversal;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventCloseContainer;
import io.hynix.events.impl.EventInventoryClose;
import io.hynix.events.impl.EventPacket;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.impl.miscellaneous.InventoryMove;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.MoveUtils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.BooleanOption;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;

import java.util.*;

@UnitRegister(name = "GuiMove", category = Category.Traversal, desc = "Позволяет ходить с открытыми контейнерами")
public class GuiMove extends Unit {
    private static final List<IPacket<?>> packet = new ArrayList<>();
    public TimerUtils wait = new TimerUtils();
    public BooleanSetting bypass = new BooleanSetting("Обход", false);

    public GuiMove() {
        this.addSettings(this.bypass);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player != null) {
            KeyBinding[] pressedKeys = new KeyBinding[]{mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump, mc.gameSettings.keyBindSprint};
            if (((Boolean)this.bypass.getValue()).booleanValue() && !this.wait.isReached(400L)) {
                for (KeyBinding keyBinding : pressedKeys) {
                    keyBinding.setPressed(false);
                }
                return;
            }
            if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen) {
                return;
            }
            this.updateKeyBindingState(pressedKeys);
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        IPacket<?> iPacket;
        if (((Boolean)this.bypass.getValue()).booleanValue() && (iPacket = e.getPacket()) instanceof CClickWindowPacket) {
            CClickWindowPacket p = (CClickWindowPacket)iPacket;
            if (MoveUtils.isMoving() && mc.currentScreen instanceof InventoryScreen) {
                this.packet.add(p);
                e.cancel();
            }
        }
    }

    @Subscribe
    public void onClose(EventInventoryClose e) {
        if (((Boolean)this.bypass.getValue()).booleanValue() && mc.currentScreen instanceof InventoryScreen && !this.packet.isEmpty() && MoveUtils.isMoving()) {
            new Thread(() -> {
                this.wait.reset();
                try {
                    Thread.sleep(300L);
                }
                catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                for (IPacket<?> p : this.packet) {
                    mc.player.connection.sendPacketWithoutEvent(p);
                }
                this.packet.clear();
            }).start();
            e.cancel();
        }
    }

    private void updateKeyBindingState(KeyBinding[] keyBindings) {
        for (KeyBinding keyBinding : keyBindings) {
            boolean isKeyPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }
}
