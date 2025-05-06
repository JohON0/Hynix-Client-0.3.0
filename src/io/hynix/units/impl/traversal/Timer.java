package io.hynix.units.impl.traversal;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.EventPacket;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.SliderSetting;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.util.math.MathHelper;

@UnitRegister(name = "Timer", category = Category.Traversal, desc = "Ускоряет игру")
public class Timer extends Unit {

    public final SliderSetting speed = new SliderSetting("Скорость", 2f, 0.1f, 10f, 0.1f);
    public final BooleanSetting smart = new BooleanSetting("Умный", true);
    public SliderSetting ticks = new SliderSetting("Тики", 1.0f, 0.15f, 3.0f, 0.1f);
    public BooleanSetting moveUp = new BooleanSetting("Восставливать", false).setVisible(() -> smart.getValue());
    public SliderSetting moveUpValue = new SliderSetting("Значение", 0.05f, 0.01f, 0.1f, 0.01f).setVisible(() -> moveUp.getValue() && smart.getValue());
    public double value;

    public float maxViolation = 100.0F;
    public float violation = 0.0F;

    public Timer() {
        addSettings(speed, ticks, smart, moveUp, moveUpValue);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SEntityVelocityPacket p) {
            if (p.getEntityID() == mc.player.getEntityId()) {
                resetSpeed();
            }
        }

        if (e.getPacket() instanceof CConfirmTransactionPacket p) {
            e.cancel();
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (!mc.player.isOnGround()) {
            this.violation += 0.1f;
            this.violation = MathHelper.clamp(this.violation, 0.0F, this.maxViolation / (this.speed.getValue()));
        }

        mc.timer.timerSpeed = this.speed.getValue();
        if (!this.smart.getValue() || mc.timer.timerSpeed <= 1.0F) {
            return;
        }
        if (this.violation < (this.maxViolation) / (this.speed.getValue())) {
            this.violation += this.ticks.getValue();
            this.violation = MathHelper.clamp(this.violation, 0.0F, this.maxViolation / (this.speed.getValue()));
        } else {
            this.resetSpeed();
        }
    }

    private void reset() {
        mc.timer.timerSpeed = 1;
    }

    public void resetSpeed() {
        this.setEnabled(false, false);
        mc.timer.timerSpeed = 1.0F;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
    }
}
