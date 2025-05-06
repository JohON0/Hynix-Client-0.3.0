package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.EventCalculateCooldown;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeListSetting;
import io.hynix.units.settings.impl.SliderSetting;
import io.hynix.utils.client.ClientUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.*;
import java.util.function.Supplier;

@FieldDefaults(level = AccessLevel.PRIVATE)
@UnitRegister(name = "ItemCooldown", category = Category.Miscellaneous,desc = "Устанавливает КД для определенных предметов")
public class ItemCooldown extends Unit {

    public static final ModeListSetting items = new ModeListSetting("Предметы",
            new BooleanSetting("Геплы", true),
            new BooleanSetting("Перки", true),
            new BooleanSetting("Хорусы", true),
            new BooleanSetting("Чарки", false));

    static final SliderSetting gappleTime = new SliderSetting("Кулдаун гепла", 4.5F, 1.0F, 10.0F, 0.05F)
            .setVisible(() -> items.is("Геплы").getValue());
    static final SliderSetting pearlTime = new SliderSetting("Кулдаун перок", 14.05F, 1.0F, 15.0F, 0.05F)
            .setVisible(() -> items.is("Перки").getValue());
    static final SliderSetting horusTime = new SliderSetting("Кулдаун хорусов", 2.3F, 1.0F, 10.0F, 0.05F)
            .setVisible(() -> items.is("Хорусы").getValue());
    static final SliderSetting enchantmentGappleTime = new SliderSetting("Кулдаун чарок", 4.5F, 1.0F, 10.0F, 0.05F)
            .setVisible(() -> items.is("Чарки").getValue());

    private final BooleanSetting onlyPvP = new BooleanSetting("Только в PVP", true);

    public ItemCooldown() {
        addSettings(items, gappleTime, pearlTime, horusTime, enchantmentGappleTime, onlyPvP);
    }

    public HashMap<Item, Long> lastUseItemTime = new HashMap<>();
    public boolean isCooldown;

    @Subscribe
    public void onCalculateCooldown(EventCalculateCooldown e) {
        applyGoldenAppleCooldown(e);
    }

    private void applyGoldenAppleCooldown(EventCalculateCooldown calcCooldown) {
        List<Item> itemsToRemove = new ArrayList<>();

        for (Map.Entry<Item, Long> entry : lastUseItemTime.entrySet()) {
            ItemEnum itemEnum = ItemEnum.getItemEnum(entry.getKey());

            if (itemEnum == null || calcCooldown.getItemStack() != itemEnum.getItem() || !itemEnum.getActive().get() || isNotPvP()) {
                continue;
            }

            long time = System.currentTimeMillis() - entry.getValue();
            float timeSetting = itemEnum.getTime().get() * 1000.0F;

            if (time < timeSetting && itemEnum.getActive().get()) {
                calcCooldown.setCooldown(time / timeSetting);
                isCooldown = true;
            } else {
                isCooldown = false;
                itemsToRemove.add(itemEnum.getItem());
            }
        }

        itemsToRemove.forEach(lastUseItemTime::remove);
    }

    public boolean isNotPvP() {
        return onlyPvP.getValue() && !ClientUtils.isPvP();
    }


    public boolean isCurrentItem(ItemEnum item) {
        if (!item.getActive().get()) {
            return false;
        }

        return item.getActive().get() && Arrays.stream(ItemEnum.values()).anyMatch(e -> e == item);
    }

    @Getter
    public enum ItemEnum {
        CHORUS(Items.CHORUS_FRUIT,
                () -> items.is("Хорусы").getValue(),
                horusTime::getValue),
        GOLDEN_APPLE(Items.GOLDEN_APPLE,
                () -> items.is("Геплы").getValue(),
                gappleTime::getValue),
        ENCHANTED_GOLDEN_APPLE(Items.ENCHANTED_GOLDEN_APPLE,
                () -> items.is("Чарки").getValue(),
                enchantmentGappleTime::getValue),
        ENDER_PEARL(Items.ENDER_PEARL,
                () -> items.is("Перки").getValue(),
                pearlTime::getValue);

        private final Item item;
        private final Supplier<Boolean> active;
        private final Supplier<Float> time;


        ItemEnum(Item item, Supplier<Boolean> active, Supplier<Float> time) {
            this.item = item;
            this.active = active;
            this.time = time;
        }

        public static ItemEnum getItemEnum(Item item) {
            return Arrays.stream(ItemEnum.values())
                    .filter(e -> e.getItem() == item)
                    .findFirst()
                    .orElse(null);
        }
    }
}
