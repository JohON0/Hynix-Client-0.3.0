package io.hynix.units.impl.combat;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.InventoryUtils;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AirItem;
import net.minecraft.item.Items;
import net.minecraft.potion.Effects;

/**
 * @author JohON0 // 30.08.24 // 14:08 (MSK+4)
 */
@UnitRegister(name = "AutoGAppleEat", category = Category.Combat,
desc = "Сам ест Золотое Яблоко при указанном здоровье")

public class AutoGappleEat extends Unit {
    private final SliderSetting healthSetting = new SliderSetting("Здоровье", 16.0f, 1.0f, 20.0f, 0.05f);
    private final BooleanSetting eatAtTheStart = new BooleanSetting("Съесть в начале", true);
    private boolean isEating;
    private final TimerUtils timerUtility = new TimerUtils();

    public AutoGappleEat() {
        addSettings(healthSetting, eatAtTheStart);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (shouldToTakeGApple() && eatAtTheStart.getValue()) {
            takeGappleInOffHand();
        }
        eatGapple();
    }


    private void eatGapple() {
        if (conditionToEat()) {
            startEating();
        } else if (isEating) {
            stopEating();
        }
    }

    private boolean shouldToTakeGApple() {
        boolean isTicksExisted = mc.player.ticksExisted == 15;
        boolean appleNotEaten = mc.player.getAbsorptionAmount() == 0.0f || !mc.player.isPotionActive(Effects.REGENERATION);
        boolean appleIsNotOffHand = mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE;
        boolean timeHasPassed = timerUtility.isReached(200);
        boolean settingIsEnalbed = eatAtTheStart.getValue();


        return (isTicksExisted && appleNotEaten && appleIsNotOffHand & timeHasPassed) && settingIsEnalbed;
    }

    private void takeGappleInOffHand() {
        int gappleSlot = InventoryUtils.getInstance().getSlotInInventory(Items.GOLDEN_APPLE);

        if (gappleSlot >= 0) {
            moveGappleToOffhand(gappleSlot);
        }
    }

    private void moveGappleToOffhand(int gappleSlot) {
        if (gappleSlot < 9 && gappleSlot != -1) {
            gappleSlot += 36;
        }
        mc.playerController.windowClick(0, gappleSlot, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
        if (!(mc.player.getHeldItemOffhand().getItem() instanceof AirItem)) {
            mc.playerController.windowClick(0, gappleSlot, 0, ClickType.PICKUP, mc.player);
        }
        timerUtility.reset();
    }

    private void startEating() {
        if (mc.currentScreen != null) {
            mc.currentScreen.passEvents = true;
        }
        if (!mc.gameSettings.keyBindUseItem.isKeyDown()) {
            mc.gameSettings.keyBindUseItem.setPressed(true);
            isEating = true;
        }
    }

    private void stopEating() {
        mc.gameSettings.keyBindUseItem.setPressed(false);
        isEating = false;
    }

    private boolean conditionToEat() {
        float myHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        boolean appleNotEaten = mc.player.getAbsorptionAmount() == 0.0f
                || !mc.player.isPotionActive(Effects.REGENERATION);

        return (isHealthLow(myHealth) || mc.player.ticksExisted < 100 && appleNotEaten)
                && hasGappleInHand()
                && !isGappleOnCooldown();
    }

    private boolean isGappleOnCooldown() {
        return mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE);
    }

    private boolean isHealthLow(float health) {
        return health <= healthSetting.getValue();
    }

    private boolean hasGappleInHand() {
        return mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_APPLE ||
                mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE;
    }

    private void reset() {
        this.timerUtility.reset();
    }

    @Override
    public void onDisable() {
        reset();
        super.onDisable();
    }
}