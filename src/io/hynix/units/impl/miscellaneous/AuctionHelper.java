package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ColorSetting;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.utils.johon0.render.color.ColorUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;

@UnitRegister(name="AuctionHelper", desc="Подсвечивает предметы в аукционе", category = Category.Miscellaneous)
public class AuctionHelper extends Unit {
//    private final ModeSetting mode = new ModeSetting("Сервер", "FunTime", "FunTime", "HolyWorld");

    private final BooleanSetting three = new BooleanSetting("Подсвечивать 3 слота", true);
    private final BooleanSetting not_ships = new BooleanSetting("Подсвечивать безшиповку", true);
    private final BooleanSetting shipi = new BooleanSetting("Подсвечивать шиповку", true);

    public final ColorSetting colorlowprice = new ColorSetting("Цвет дешевого", ColorUtils.rgb(0,255,0));
    public final ColorSetting colormedprice = new ColorSetting("Цвет среднего", ColorUtils.rgb(255,255,0));
    public final ColorSetting colorhighprice = new ColorSetting("Цвет дорогого", ColorUtils.rgb(255,0,0));

    public final ColorSetting colorunship = new ColorSetting("Цвет безшиповки", ColorUtils.rgb(0,255,0));
    public final ColorSetting colorship = new ColorSetting("Цвет шиповки", ColorUtils.rgb(255,255,0));

    @Getter @Setter
    private float x;
    @Getter @Setter
    private float y;
    @Getter @Setter
    private float x2;
    @Getter @Setter
    private float y2;
    @Getter @Setter
    private float x3;
    @Getter @Setter
    private float y3;
    @Getter @Setter
    private float x4;
    @Getter @Setter
    private float y4;
    @Getter @Setter
    private float x5;
    @Getter @Setter
    private float y5;

    public AuctionHelper() {
        this.addSettings(
                //mode,
                three, not_ships, shipi,
                colorlowprice,
                colormedprice,
                colorhighprice,
                colorunship,
                colorship);
    }

    @Subscribe
    public void onUpdate(EventUpdate update) {
        Screen screen = mc.currentScreen;
        if (screen instanceof ChestScreen) {
            ChestScreen e = (ChestScreen) screen;
            if (e.getTitle().getString().contains("Аукцион") || e.getTitle().getString().contains("Поиск:")) {
                Container container = e.getContainer();
                Slot slot1 = null;
                Slot slot2 = null;
                Slot slot3 = null;
                Slot slot4 = null;
                Slot slot5 = null;
                int fsPrice = Integer.MAX_VALUE;
                int medPrice = Integer.MAX_VALUE;
                int thPrice = Integer.MAX_VALUE;

                for (Slot slot : container.inventorySlots) {
                    if (slot.slotNumber > 44) continue;
                    int currentPrice = this.extractPriceFromStack(slot.getStack());
//                    System.out.println(currentPrice);
                    boolean ship = this.extractEnchantmentFromStack(slot.getStack());
                    if (currentPrice != -1 && currentPrice < fsPrice) {
                        fsPrice = currentPrice;
                        slot1 = slot;
                    }
                    if (not_ships.getValue()) {
                        if (ship && slot.getStack().getItem() instanceof ArmorItem) {
                            slot4 = slot;
                        }
                    }
                    if (shipi.getValue()) {
                        if (!ship && slot.getStack().getItem() instanceof ArmorItem) {
                            slot5 = slot;
                        }
                    }
                    if (three.getValue()) {
                        if (currentPrice != -1 && currentPrice < medPrice && currentPrice > fsPrice) {
                            medPrice = currentPrice;
                            slot2 = slot;
                        }
                        if (currentPrice != -1 && currentPrice < thPrice && currentPrice > medPrice) {
                            thPrice = currentPrice;
                            slot3 = slot;
                        }
                    }
                }
                setSlotPositions(slot1, slot2, slot3, slot4, slot5);
            } else {
                resetSlotPositions();
            }
        } else {
            resetSlotPositions();
        }
    }

    private void setSlotPositions(Slot slot1, Slot slot2, Slot slot3, Slot slot4, Slot slot5) {
        if (slot1 != null) {
            setX(slot1.xPos);
            setY(slot1.yPos);
        }
        if (slot2 != null) {
            setX2(slot2.xPos);
            setY2(slot2.yPos);
        }
        if (slot3 != null) {
            setX3(slot3.xPos);
            setY3(slot3.yPos);
        }
        if (slot4 != null) {
            setX4(slot4.xPos);
            setY4(slot4.yPos);
        }
        if (slot5 != null) {
            setX5(slot5.xPos);
            setY5(slot5.yPos);
        }
    }

    private void resetSlotPositions() {
        setX(0.0f);
        setX2(0.0f);
        setX3(0.0f);
        setX4(0.0f);
        setX5(0.0f);
    }

    protected int extractPriceFromStack(ItemStack stack) {
        CompoundNBT display;
        CompoundNBT tag = stack.getTag();
//        if (mode.is("FunTime")) {
            if (tag != null && tag.contains("display", 10) && (display = tag.getCompound("display")).contains("Lore", 9)) {
                ListNBT lore = display.getList("Lore", 8);
                for (int j = 0; j < lore.size(); ++j) {
                    JsonObject object = JsonParser.parseString(lore.getString(j)).getAsJsonObject();
                    if (object.has("extra") && object.getAsJsonArray("extra").size() > 2) {
                        JsonObject title = object.getAsJsonArray("extra").get(1).getAsJsonObject();
                        if (title.get("text").getAsString().trim().toLowerCase().contains("ценa")) {
                            String line = object.getAsJsonArray("extra").get(2).getAsJsonObject().get("text").getAsString()
                                    .trim().substring(1).replaceAll(",", "").replaceAll(" ", "");
                            return Integer.parseInt(line);
                        }
                    }
                }
            }
//        } else {
//            if (tag != null && tag.contains("display", 10)) {
//                // Проверяем, что тег не пустой и содержит "display"
//                // Проверяем, есть ли "Lore" в теге
//                if (tag.contains("Lore", 9)) {
//                    // Получаем список Lore (описания) предмета
//                    ListNBT lore = tag.getList("Lore", 8);
//
//                    // Проверяем, что есть как минимум 4 строки в Lore
//                    if (lore.size() >= 4) {
//                        // Извлекаем четвёртую строку Lore (индекс 3)
//                        String line = ITextComponent.Serializer.getComponentFromJson(lore.getString(3)).getString();
//
//                        // Проверяем, содержит ли строка "Цена:"
//                        if (line.contains("Цена:")) {
//                            // Извлекаем часть строки после "Цена:"
//                            String priceStr = line.substring(line.indexOf(":") + 1)
//                                    .replaceAll("\\s+", "") // Удаляем все пробелы
//                                    .replaceAll("[^\\d]", "") // Удаляем все нечисловые символы
//
//                                    .trim(); // Убираем лишние пробелы по краям
//
//                            // Превращаем строку цены в целое число и возвращаем его
//                            return Integer.parseInt(priceStr);
//                        }
//                    }
//                }
//            }
//        }
        return -1;

    }
//    protected int extractPriceFromStack(ItemStack stack) {
//        CompoundNBT display;
//        CompoundNBT tag = stack.getTag();
//        if (tag != null && tag.contains("display", 10) && (display = tag.getCompound("display")).contains("Lore", 9)) {
//            ListNBT lore = display.getList("Lore", 8);
//            for (int j = 0; j < lore.size(); ++j) {
//                JsonObject object = JsonParser.parseString(lore.getString(j)).getAsJsonObject();
//                if (object.has("extra") && object.getAsJsonArray("extra").size() > 2) {
//                    JsonObject title = object.getAsJsonArray("extra").get(1).getAsJsonObject();
//                    if (title.get("text").getAsString().trim().toLowerCase().contains("ценa")) {
//                        String line = object.getAsJsonArray("extra").get(2).getAsJsonObject().get("text").getAsString().trim().substring(1).replaceAll(",", "").replaceAll(" ", "");
//                        return Integer.parseInt(line);
//                    }
//                }
//            }
//        }
//        return -1;
//    }

    protected boolean extractEnchantmentFromStack(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem) {
            CompoundNBT tag = stack.getTag();
            String stringtag = tag != null ? tag.getString() : "";
            return !stringtag.contains("thorns");
        }
        return false;
    }
}
