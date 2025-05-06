package io.hynix.units.impl.combat;

import com.google.common.eventbus.Subscribe;

import io.hynix.HynixMain;
import io.hynix.events.impl.EventMotion;
import io.hynix.events.impl.EventUpdate;
import io.hynix.ui.clickgui.components.settings.MultiBoxComponent;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeListSetting;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.MoveUtils;
import io.hynix.utils.player.PotionUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.potion.*;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@UnitRegister(name = "AutoPotionUse", category = Category.Combat, desc = "Сам использует Зелья")
public class AutoPotionUse extends Unit {
    private static ModeListSetting potions = new ModeListSetting("Бафать",
            new BooleanSetting("Силу", true),
            new BooleanSetting("Скорость", true),
            new BooleanSetting("Огнестойкость", true),
            new BooleanSetting("Исцеление", true)); // Добавлено зелье исцеления

    private BooleanSetting smart = new BooleanSetting("Умный", false);
    private BooleanSetting autoDisable = new BooleanSetting("Авто выключение", false);
    private BooleanSetting onlyPvP = new BooleanSetting("Только в PVP", false);
    private SliderSetting healthThreshold = new SliderSetting("Порог здоровья", 5, 1, 20,1).setVisible(() -> potions.is("Исцеление").getValue()); // Слайдер для порога здоровья

    public boolean isActive;
    private int selectedSlot;
    private float previousPitch;
    private TimerUtils time = new TimerUtils(), time2 = new TimerUtils();
    private PotionUtils potionUtil = new PotionUtils();
    public boolean isActivePotion;

    public AutoPotionUse() {
        this.addSettings(potions, smart, onlyPvP, autoDisable, healthThreshold);
    }

    boolean canThrow() {
        boolean canThrow = !smart.getValue();
        if (smart.getValue()) {
            Iterable<Entity> list = mc.world.getAllEntities();
            List<PlayerEntity> playerEntityList = new ArrayList<>();
            list.forEach(e -> {
                if (e instanceof PlayerEntity && e != mc.player && !HynixMain.getInstance().getFriendManager().isFriend(e.getName().getString())) {
                    playerEntityList.add((PlayerEntity) e);
                }
            });
            playerEntityList.sort(Comparator.comparingDouble(p -> mc.player.getDistance(p)));
            if (!playerEntityList.isEmpty())
                canThrow = playerEntityList.get(0).getDistance(mc.player) < 16;
        }
        return canThrow;
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (this.isActive() && this.shouldUsePotion()) {
            for (PotionType potionType : PotionType.values()) {
                isActivePotion = potionType.isEnabled();
            }
        } else {
            isActivePotion = false;
        }

        if (this.isActive() && this.shouldUsePotion() && previousPitch == mc.player.getLastReportedPitch()) {
            int oldItem = mc.player.inventory.currentItem;
            this.selectedSlot = -1;

            // Использование зелья исцеления при определённом уровне здоровья
            if (healthThreshold.getValue() > 0 && mc.player.getHealth() <= healthThreshold.getValue()) {
                this.selectedSlot = findPotionSlot(PotionType.HEALING);
                if (this.selectedSlot != -1) {
                    PotionUtils.useItem(Hand.MAIN_HAND);
                    return; // Прекратить выполнение, после использования зелья исцеления
                }
            }

            for (PotionType potionType : PotionType.values()) {
                if (potionType.isEnabled() && canThrow()) {
                    int slot = this.findPotionSlot(potionType);
                    if (this.selectedSlot == -1) {
                        this.selectedSlot = slot;
                    }
                    this.isActive = true;
                }
            }

            if (this.selectedSlot > 8) {
                mc.playerController.pickItem(this.selectedSlot);
            }

            mc.player.connection.sendPacket(new CHeldItemChangePacket(oldItem));
        }

        if (time.hasTimeElapsed(500L)) {
            try {
                this.reset();
                this.selectedSlot = -2;
            } catch (Exception ignored) {
            }
        }

        this.potionUtil.changeItemSlot(this.selectedSlot == -2);
        if (this.autoDisable.getValue() && this.isActive && this.selectedSlot == -2) {
            toggle();
            this.isActive = false;
        }
    }

    @Subscribe
    private void onMotion(EventMotion e) {
        if (!this.isActive() || !this.shouldUsePotion() || !canThrow()) {
            return;
        }

        float[] angles = new float[]{mc.player.rotationYaw, 90.0F};
        this.previousPitch = 90.0F;
        e.setYaw(angles[0]);
        e.setPitch(this.previousPitch);
        mc.player.rotationPitchHead = this.previousPitch;
        mc.player.rotationYawHead = angles[0];
        mc.player.renderYawOffset = angles[0];
    }

    private boolean shouldUsePotion() {
        return !(onlyPvP.getValue() && !ClientUtils.isPvP());
    }

    private void reset() {
        for (PotionType potionType : PotionType.values()) {
            if (potionType.isPotionSettingEnabled().get()) {
                potionType.setEnabled(this.isPotionActive(potionType));
            }
        }
    }

    private int findPotionSlot(PotionType type) {
        int hbSlot = this.getPotionIndexHb(type.getPotionId());

        if (hbSlot != -1) {
            this.potionUtil.setPreviousSlot(mc.player.inventory.currentItem);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            if(time2.hasTimeElapsed(ThreadLocalRandom.current().nextInt(100,130),true)||!smart.getValue()){
                PotionUtils.useItem(Hand.MAIN_HAND);
                type.setEnabled(false);
                time.reset();
            }
            return hbSlot;
        } else {
            int invSlot = this.getPotionIndexInv(type.getPotionId());
            if (invSlot != -1) {
                this.potionUtil.setPreviousSlot(mc.player.inventory.currentItem);
                mc.playerController.pickItem(invSlot);
                if(time2.hasTimeElapsed(ThreadLocalRandom.current().nextInt(100,130),true)||!smart.getValue()){
                    PotionUtils.useItem(Hand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                    type.setEnabled(false);
                    time.reset();
                }
                return invSlot;
            } else {
                return -1;
            }
        }
    }

    public boolean isActive() {
        for (PotionType potionType : PotionType.values()) {
            if (potionType.isPotionSettingEnabled().get() && potionType.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    private boolean isPotionActive(PotionType type) {
        if (mc.player.isPotionActive(type.getPotion())) {
            this.isActive = false;
            return false;
        } else {
            return this.getPotionIndexInv(type.getPotionId()) != -1 || this.getPotionIndexHb(type.getPotionId()) != -1;
        }
    }

    private int getPotionIndexHb(int id) {
        for (int i = 0; i < 9; ++i) {
            for (EffectInstance potion : net.minecraft.potion.PotionUtils.getEffectsFromStack(mc.player.inventory.getStackInSlot(i))) {
                if (potion.getPotion() == Effect.get(id) && mc.player.inventory.getStackInSlot(i).getItem() == Items.SPLASH_POTION) {
                    return i;
                }
            }
        }

        return -1;
    }

    private int getPotionIndexInv(int id) {
        for (int i = 9; i < 36; ++i) {
            for (EffectInstance potion : net.minecraft.potion.PotionUtils.getEffectsFromStack(mc.player.inventory.getStackInSlot(i))) {
                if (potion.getPotion() == Effect.get(id) && mc.player.inventory.getStackInSlot(i).getItem() == Items.SPLASH_POTION) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public void onDisable() {
        this.isActive = false;
        super.onDisable();
    }

    enum PotionType {
        STRENGTH(Effects.STRENGTH, 5, () -> potions.get(0).getValue()),
        SPEED(Effects.SPEED, 1, () -> potions.get(1).getValue()),
        FIRE_RESIST(Effects.FIRE_RESISTANCE, 12, () -> potions.get(2).getValue()),
        HEALING(Effects.INSTANT_HEALTH, 6, () -> potions.get(3).getValue()); // Добавлено зелье исцеления

        private final Effect potion;
        private final int potionId;
        private final Supplier<Boolean> potionSetting;
        private boolean enabled;

        PotionType(Effect potion, int potionId, Supplier<Boolean> potionSetting) {
            this.potion = potion;
            this.potionId = potionId;
            this.potionSetting = potionSetting;
        }

        public Effect getPotion() {
            return this.potion;
        }

        public int getPotionId() {
            return this.potionId;
        }

        public Supplier<Boolean> isPotionSettingEnabled() {
            return this.potionSetting;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean var1) {
            this.enabled = var1;
        }
    }
}
