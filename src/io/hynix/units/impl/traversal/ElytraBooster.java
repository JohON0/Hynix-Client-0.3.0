package io.hynix.units.impl.traversal;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventMoving;
import io.hynix.events.impl.EventPacket;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;

@UnitRegister(name = "ElytraBooster", category = Category.Traversal, desc = "Ускоряет вас на элитре")
public class ElytraBooster extends Unit {
    private final BooleanSetting noBallSwitch = new BooleanSetting("Умный обгон", false);
    private final BooleanSetting antiflag = new BooleanSetting("Выключить при флаге", false);

    public ElytraBooster() {
        this.addSettings(this.noBallSwitch, this.antiflag);
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0f;

        super.onDisable();
    }
    @Subscribe
    public void onPacket(EventPacket e) {
        if (antiflag.getValue()) {
            if (e.getPacket() instanceof SPlayerPositionLookPacket p) {
                mc.player.setPacketCoordinates(p.getX(), p.getY(), p.getZ());
                mc.player.setRawPosition(p.getX(), p.getY(), p.getZ());
                toggle();
            }
        }
    }
    @Subscribe
    public void onMove(EventMoving e) {
        mc.timer.timerSpeed = !mc.player.isElytraOfCape() && mc.player.isElytraFlying() && (Boolean) this.noBallSwitch.getValue() ? 1.011f : 1.0f;
    }
}

