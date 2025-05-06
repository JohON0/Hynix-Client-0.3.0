package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.johon0.animations.easing.CompactAnimation;
import io.hynix.utils.johon0.animations.easing.Easing;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
/**
 * @author JohON0
 */
@UnitRegister(name = "FullBright", category = Category.Display, desc = "Полнстью делает мир светлым (даже в темное время суток)")
public class FullBright extends Unit {
    private final ModeSetting mode = new ModeSetting("Мод", "Gamma", "Gamma", "Potion");
    private final CompactAnimation animation = new CompactAnimation(Easing.EASE_OUT_QUART, 500);
    private final BooleanSetting dynamic = new BooleanSetting("Динамический", false).setVisible(() -> mode.is("Gamma"));
    private final SliderSetting bright = new SliderSetting("Яркость", 2.5f, 1, 5, 0.1f).setVisible(() -> !dynamic.getValue() && mode.is("Gamma"));

    private float originalGamma;
    private boolean isGammaChanged = false;

    public FullBright() {
        addSettings(mode, dynamic, bright);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        saveGamma();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        restoreGamma();
        mc.player.removeActivePotionEffect(new EffectInstance(Effects.NIGHT_VISION).getPotion());
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mode.is("Gamma")) {
            mc.player.removeActivePotionEffect(new EffectInstance(Effects.NIGHT_VISION).getPotion());
            if (dynamic.getValue()) {
                float lightLevel = mc.player.getBrightness();
                animation.run(calculateGamma(lightLevel));
                float gamma = (float) animation.getValue();
                setGamma(gamma);
            } else {
                setGamma(bright.getValue());
            }
        } else {
            mc.player.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 16360, 0));
        }
    }

    private float calculateGamma(float lightLevel) {
        float minGamma = 0.5f;
        float maxGamma = 5.0f;
        float gammaRange = maxGamma - minGamma;
        float lightRange = 1;
        float gamma = minGamma + (gammaRange * (1.0f - lightLevel / lightRange));
        return gamma;
    }

    public void saveGamma() {
        originalGamma = (float) mc.gameSettings.gamma;
    }

    public void setGamma(float newGamma) {
        saveGamma();
        mc.gameSettings.gamma = newGamma;
        isGammaChanged = true;
    }

    public void restoreGamma() {
        if (isGammaChanged) {
            mc.gameSettings.gamma = originalGamma;
            isGammaChanged = false;
        }
    }
}
