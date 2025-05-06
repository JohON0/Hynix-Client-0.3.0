package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;
import io.hynix.HynixMain;
import io.hynix.events.impl.EventPacket;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.impl.traversal.NoSlowDown;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;

@UnitRegister(name = "NoServerDesync", category = Category.Miscellaneous, desc = "Убирает синхронизацию с сервером")
public class NoServerDesync extends Unit {

    private float targetYaw;
    private float targetPitch;
    private boolean isPacketSent;

    @Subscribe
    private void onPacket(EventPacket e) {
        IPacket<?> iPacket = e.getPacket();
        NoSlowDown noSlowDown = HynixMain.getInstance().getModuleManager().getNoSlowDown();
        if (mc.player == null) return;
        if (noSlowDown.isEnabled() && noSlowDown.noSlowMode.is("GrimLast")) {
            SHeldItemChangePacket wrapper;
            int serverSlot;
            if (mc.player != null && (iPacket = e.getPacket()) instanceof SHeldItemChangePacket && (serverSlot = (wrapper = (SHeldItemChangePacket)iPacket).getHeldItemHotbarIndex()) != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(Math.max(mc.player.inventory.currentItem - 1, 0)));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                e.cancel();
            }
        } else {
            if (iPacket instanceof SHeldItemChangePacket wrapper) {
                final int serverSlot = wrapper.getHeldItemHotbarIndex();
                if (serverSlot != mc.player.inventory.currentItem) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                    e.cancel();
                }
            }
        }
        if (e.getPacket() instanceof SPlayerPositionLookPacket && e.isSend()) {
            SPlayerPositionLookPacket packet = (SPlayerPositionLookPacket)iPacket;
            packet.setYaw(mc.player == null ? packet.getYaw() : mc.player.rotationYaw);
            packet.setPitch(mc.player == null ? packet.getPitch() : mc.player.rotationPitch);
        }
    }
}
