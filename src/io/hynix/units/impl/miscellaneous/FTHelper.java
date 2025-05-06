package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;

import io.hynix.HynixMain;
import io.hynix.events.impl.EventKey;
import io.hynix.events.impl.EventPacket;
import io.hynix.events.impl.EventUpdate;
import io.hynix.ui.hud.impl.NotificationRender;
import io.hynix.ui.notifications.NotificationManager;
import io.hynix.ui.notifications.NotificationType;
import io.hynix.ui.notifications.impl.SuccessNotify;
import io.hynix.ui.notifications.impl.WarningNotify;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.impl.display.Notifications;
import io.hynix.units.settings.api.Setting;
import io.hynix.units.settings.impl.BindSetting;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeListSetting;
import io.hynix.utils.johon0.math.TimerUtils;
import io.hynix.utils.player.InventoryUtils;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AirItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

import java.util.Locale;


@UnitRegister(
        name = "FTHelper",
        category = Category.Miscellaneous,
        desc = "Помощник для сервера FunTime"
)
public class FTHelper extends Unit {
    private final ModeListSetting mode = new ModeListSetting("Тип",
            new BooleanSetting("Использование по бинду", true),
            new BooleanSetting("Конвертировать время", true),
            new BooleanSetting("Закрывать меню", true));

    private final BindSetting disorientationKey = (new BindSetting("Кнопка дезориентации", -1)).setVisible(() -> this.mode.is("Использование по бинду").getValue());
    private final BindSetting trapKey = (new BindSetting("Кнопка трапки", -1)).setVisible(() -> this.mode.is("Использование по бинду").getValue());
    private final BindSetting plastKey = (new BindSetting("Кнопка пласта", -1)).setVisible(() -> this.mode.is("Использование по бинду").getValue());
    private final BindSetting pilKey = (new BindSetting("Кнопка явной пыли", -1)).setVisible(() -> this.mode.is("Использование по бинду").getValue());
    private final BindSetting arbKey = (new BindSetting("Кнопка арбалета", -1)).setVisible(() -> this.mode.is("Использование по бинду").getValue());
    private final BindSetting aurakey = (new BindSetting("Божья аура", -1)).setVisible(() -> this.mode.is("Использование по бинду").getValue());
    private final BindSetting flashKey = (new BindSetting("Моча Флеша", -1)).setVisible(() -> this.mode.is("Использование по бинду").getValue());
    private final BindSetting otrigaKey = (new BindSetting("Зелье Отрыжки", -1)).setVisible(() -> this.mode.is("Использование по бинду").getValue());
    private final BindSetting serkaKey = (new BindSetting("Серная Кислота", -1)).setVisible(() -> this.mode.is("Использование по бинду").getValue());
    private final BindSetting shulker = (new BindSetting("Открыть Шалкер", -1)).setVisible(() -> this.mode.is("Использование по бинду").getValue());

    InventoryUtils.Hand handUtil = new InventoryUtils.Hand();

    long delay;
    boolean disorientationThrow;
    boolean shulkeruse;
    boolean trapThrow;
    boolean plastThrow;
    boolean pilThrow;
    boolean auraThrow;
    boolean arbThrow;
    boolean flashThrow;
    boolean otrigaThrow;
    boolean serkaThrow;

    public FTHelper() {
        this.addSettings(this.mode, this.disorientationKey, this.trapKey, this.plastKey,
                this.pilKey, this.aurakey, this.arbKey, this.flashKey, this.otrigaKey,
                this.serkaKey, this.shulker);
    }

    @Subscribe
    private void onKey(EventKey e) {
        if (e.getKey() == (Integer)this.shulker.getValue()) {
            this.shulkeruse = true;
        }
        if (e.getKey() == (Integer)this.disorientationKey.getValue()) {
            this.disorientationThrow = true;
        }
        if (e.getKey() == (Integer)this.trapKey.getValue()) {
            this.trapThrow = true;
        }

        if (e.getKey() == (Integer)this.plastKey.getValue()) {
            this.plastThrow = true;
        }

        if (e.getKey() == (Integer)this.pilKey.getValue()) {
            this.pilThrow = true;
        }

        if (e.getKey() == (Integer)this.arbKey.getValue()) {
            this.arbThrow = true;
        }

        if (e.getKey() == (Integer)this.flashKey.getValue()) {
            this.flashThrow = true;
        }

        if (e.getKey() == (Integer)this.otrigaKey.getValue()) {
            this.otrigaThrow = true;
        }

        if (e.getKey() == (Integer)this.serkaKey.getValue()) {
            this.serkaThrow = true;
        }

        if (e.getKey() == (Integer)this.aurakey.getValue()) {
            this.auraThrow = true;
        }

    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        Notifications notifications = HynixMain.getInstance().getModuleManager().getNotifications();
        int hbSlot;
        int invSlot;
        int slot;
        if (this.disorientationThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 500L);
            hbSlot = this.getItemForName("дезориентация", true);
            invSlot = this.getItemForName("дезориентация", false);
            if (invSlot == -1 && hbSlot == -1) {
                this.print("Дезориентация не найдена!");


                this.disorientationThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_EYE)) {
                this.print("Заюзал дезориентацию!");


                slot = this.findAndTrowItem(hbSlot, invSlot);
                if (slot > 9) {
                    mc.playerController.pickItem(slot);
                }
            }

            this.disorientationThrow = false;
        }
        if (shulkeruse) {
            int id = -1;
            for(int i=0;i<45;i++){
                ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

                if ((ShulkerBoxBlock.getBlockFromItem(itemStack.getItem()) instanceof ShulkerBoxBlock)) {
                    id = i;
                    break;
                }
            }
            if(id!=-1){
                ItemStack itemStack = mc.player.inventory.getStackInSlot(id);
                mc.getConnection().sendPacket(new CClickWindowPacket(0, id, 1, ClickType.PICKUP, itemStack, mc.player.openContainer.getNextTransactionID(mc.player.inventory)));

                // ебейшая симуляция закрытия инва
                mc.getConnection().sendPacket(new CCloseWindowPacket());
            }
            for(int i=0;i<9;i++){
                ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

                if ((ShulkerBoxBlock.getBlockFromItem(itemStack.getItem()) instanceof ShulkerBoxBlock)) {
                    int last = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = id;
                    mc.player.connection.sendPacket((IPacket)new CHeldItemChangePacket(id));
                    mc.player.connection.sendPacket((IPacket)new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    mc.player.inventory.currentItem = last;
                    mc.player.connection.sendPacket((IPacket)new CHeldItemChangePacket(last));
                    break;
                }
            }
            shulkeruse = false;
        }
        if (this.plastThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 500L);
            hbSlot = this.getItemForName("пласт", true);
            invSlot = this.getItemForName("пласт", false);
            if (invSlot == -1 && hbSlot == -1) {
                this.print("Пласт не найден!");


                this.plastThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.DRIED_KELP)) {
                this.print("Заюзал пласт!");


                slot = this.findAndTrowItem(hbSlot, invSlot);
                if (slot > 9) {
                    mc.playerController.pickItem(slot);
                }
            }

            this.plastThrow = false;
        }

        if (this.trapThrow) {
            hbSlot = this.getItemForName("трапка", true);
            invSlot = this.getItemForName("трапка", false);
            if (invSlot == -1 && hbSlot == -1) {
                this.print("Трапка не найдена");


                this.trapThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.NETHERITE_SCRAP)) {
                this.print("Заюзал трапку!");


                slot = mc.player.inventory.currentItem;
                slot = this.findAndTrowItem(hbSlot, invSlot);
                if (slot > 9) {
                    mc.playerController.pickItem(slot);
                }

                if (InventoryUtils.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != slot) {
                    mc.player.inventory.currentItem = slot;
                }
            }

            this.trapThrow = false;
        }

        if (this.pilThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 500L);
            hbSlot = this.getItemForName("явная пыль", true);
            invSlot = this.getItemForName("явная пыль", false);
            if (invSlot == -1 && hbSlot == -1) {
                this.print("Явная пыль не найдена!");


                this.pilThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.SUGAR)) {
                this.print("Заюзал явную пыль!");


                slot = this.findAndTrowItem(hbSlot, invSlot);
                if (slot > 9) {
                    mc.playerController.pickItem(slot);
                }
            }

            this.pilThrow = false;
        }

        if (this.arbThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 500L);
            hbSlot = this.getItemForName("арбалет крушителя", true);
            invSlot = this.getItemForName("арбалет крушителя", false);
            if (invSlot == -1 && hbSlot == -1) {
                this.print("Арбалет не найден!");


                this.arbThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.CROSSBOW)) {
                this.print("Заюзал арбалет!");


                slot = this.findAndTrowItem(hbSlot, invSlot);
                if (slot > 9) {
                    mc.playerController.pickItem(slot);
                }
            }

            this.arbThrow = false;
        }

        if (this.flashThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 500L);
            hbSlot = this.getItemForName("флеша", true);
            invSlot = this.getItemForName("флеша", false);
            if (invSlot == -1 && hbSlot == -1) {
                this.print("Флешка не найдена!");


                this.flashThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.SPLASH_POTION)) {
                this.print("Заюзал флешку!");


                slot = this.findAndTrowItem(hbSlot, invSlot);
                if (slot > 9) {
                    mc.playerController.pickItem(slot);
                }
            }

            this.flashThrow = false;
        }

        if (this.otrigaThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 500L);
            hbSlot = this.getItemForName("отрыжки", true);
            invSlot = this.getItemForName("отрыжки", false);
            if (invSlot == -1 && hbSlot == -1) {
                this.print("Отрыжка не найдена!");


                this.otrigaThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.SPLASH_POTION)) {
                this.print("Заюзал отрыжку!");


                slot = this.findAndTrowItem(hbSlot, invSlot);
                if (slot > 9) {
                    mc.playerController.pickItem(slot);
                }
            }

            this.otrigaThrow = false;
        }

        if (this.serkaThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 500L);
            hbSlot = this.getItemForName("серная", true);
            invSlot = this.getItemForName("серная", false);
            if (invSlot == -1 && hbSlot == -1) {
                this.print("Серка не найдена!");


                this.serkaThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.SPLASH_POTION)) {
                this.print("Заюзал серку!");


                slot = this.findAndTrowItem(hbSlot, invSlot);
                if (slot > 9) {
                    mc.playerController.pickItem(slot);
                }
            }

            this.serkaThrow = false;
        }

        if (this.auraThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 500L);
            hbSlot = this.getItemForName("божья", true);
            invSlot = this.getItemForName("божья", false);
            if (invSlot == -1 && hbSlot == -1) {
                this.print("Аура не найдена!");


                this.auraThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.PHANTOM_MEMBRANE)) {
                this.print("Заюзал ауру!");


                slot = this.findAndTrowItem(hbSlot, invSlot);
                if (slot > 9) {
                    mc.playerController.pickItem(slot);
                }
            }

            this.auraThrow = false;
        }

        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 500L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
        if ((Boolean)this.mode.is("Конвертировать время").getValue()) {
            IPacket var3 = e.getPacket();
            if (var3 instanceof SChatPacket) {
                SChatPacket chatPacket = (SChatPacket)var3;
                String chatMessage = chatPacket.getChatComponent().getString().toLowerCase(Locale.ROOT);
                if (chatMessage.contains("до следующего ивента:")) {
                    int startIndex = chatMessage.indexOf(":") + 2;
                    int endIndex = chatMessage.indexOf(" сек", startIndex);
                    if (endIndex == -1) {
                        return;
                    }

                    String secondsString = chatMessage.substring(startIndex, endIndex);
                    int seconds = Integer.parseInt(secondsString.trim());
                    String convertedTime = this.convertTime(seconds);
                    HynixMain.getInstance().getNotifyManager().add(0, new SuccessNotify("До следующего ивента: " + TextFormatting.RED + convertedTime, 5000));

                }
            }
        }

    }

    private String convertTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = seconds % 3600 / 60;
        int remainingSeconds = seconds % 60;
        String timeString = "";
        if (hours > 0) {
            timeString = timeString + hours + " часов ";
        }

        if (minutes > 0) {
            timeString = timeString + minutes + " минут ";
        }

        if (remainingSeconds > 0 || timeString.isEmpty()) {
            timeString = timeString + remainingSeconds + " секунд";
        }

        return timeString.trim();
    }

    private int findAndTrowItem(int hbSlot, int invSlot) {
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            this.delay = System.currentTimeMillis();
            return hbSlot;
        } else if (invSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            this.delay = System.currentTimeMillis();
            return invSlot;
        } else {
            return -1;
        }
    }

    public void onDisable() {
        this.disorientationThrow = false;
        this.trapThrow = false;
        this.plastThrow = false;
        this.arbThrow = false;
        this.flashThrow = false;
        this.otrigaThrow = false;
        this.serkaThrow = false;
        this.auraThrow = false;
        this.delay = 500L;
        super.onDisable();
    }

    private int getItemForName(String name, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;

        for(int i = firstSlot; i < lastSlot; ++i) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (!(itemStack.getItem() instanceof AirItem)) {
                String displayName = TextFormatting.getTextWithoutFormattingCodes(itemStack.getDisplayName().getString());
                if (displayName != null && displayName.toLowerCase().contains(name)) {
                    return i;
                }
            }
        }

        return -1;
    }
}