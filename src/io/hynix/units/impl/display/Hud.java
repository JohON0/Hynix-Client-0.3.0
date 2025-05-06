package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;

import io.hynix.HynixMain;
import io.hynix.events.impl.AttackEvent;
import io.hynix.events.impl.EventRender2D;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.managers.drag.Dragging;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ColorSetting;
import io.hynix.units.settings.impl.ModeListSetting;
import io.hynix.units.settings.impl.ModeSetting;
import io.hynix.ui.hud.impl.*;
import io.hynix.utils.johon0.render.color.ColorUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;
/**
 * @author attack.dev
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@UnitRegister(name = "Hud", category = Category.Display,desc = "Интерфейс чита")
public class Hud extends Unit {

    public final ModeListSetting elements = new ModeListSetting("Элементы",
            new BooleanSetting("Ватермарка", true),
            new BooleanSetting("Лист эффектов", true),
            new BooleanSetting("Лист модеров", true),
            new BooleanSetting("Лист биндов", true),
            new BooleanSetting("Лист событий (ReallyWorld)", false),
            new BooleanSetting("Таргет худ", true),
            new BooleanSetting("Таймер", false),
            new BooleanSetting("Информация", false),
            new BooleanSetting("Броня", true),
            new BooleanSetting("Список Функций", false),
            new BooleanSetting("Задержка предметов", false)


    );

    public final BooleanSetting particlesOnTarget = new BooleanSetting("Партиклы", true).setVisible(() -> elements.is("Таргет худ").getValue());
    public BooleanSetting fixhp = new BooleanSetting("Фикс хп", false).setVisible(() -> elements.is("Таргет худ").getValue());

    public static final ModeSetting themeMode = new ModeSetting("Палитра темы", "Шаблон", "Шаблон", "Кастом");

    public static final ColorSetting themeColor = new ColorSetting("Цвет темы", new Color(202, 22, 22).getRGB()).setVisible(() -> themeMode.is("Кастом"));

    public static final ModeSetting theme = new ModeSetting("Тема", "Красная",
            "Зеленая", "Красная", "Темно-красная", "Синяя", "Темно-синяя", "Желтая", "Оранжевая", "Фиолетовая"
    ).setVisible(() -> themeMode.is("Шаблон"));

    final Watermark watermark;
    final PotionHud potionHud;
    final TimerHud timerHud;
    final Keybinds keybinds;
    final TargetHud targetHud;
    final ArmorHud armorHud;
    final StaffHud staffHud;
    final Schedules schedules;
    final NotificationRender notification;
    final ClientInfo clientInfo;
    final ArrayList arrayList;
    final Cooldowns cooldowns;

    @Getter
    private final CopyOnWriteArrayList<TargetHud.HeadParticle> particles = new CopyOnWriteArrayList();

    @Subscribe
    private void onAttack(AttackEvent e) {
        if (elements.is("Таргет худ").getValue() && particlesOnTarget.getValue()) {
            targetHud.onAttack(e);
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.gameSettings.showDebugInfo) {
            return;
        }

        if (elements.is("Лист модеров").getValue()) staffHud.update(e);
        if (elements.is("Таймер").getValue()) timerHud.update(e);
        if (elements.is("Лист событий (ReallyWorld)").getValue()) schedules.update(e);
        if (elements.is("Список Функций").getValue()) arrayList.update(e);

    }


    @Subscribe
    private void onDisplay(EventRender2D e) {
        if (mc.gameSettings.showDebugInfo || e.getType() != EventRender2D.Type.POST) {
            return;
        }

        if (elements.is("Лист событий (ReallyWorld)").getValue()) schedules.render(e);
        if (elements.is("Список Функций").getValue()) arrayList.render(e);
        if (elements.is("Информация").getValue()) clientInfo.render(e);
        if (elements.is("Лист эффектов").getValue()) potionHud.render(e);
        if (elements.is("Лист биндов").getValue()) keybinds.render(e);
        if (elements.is("Лист модеров").getValue()) staffHud.render(e);
        if (elements.is("Таргет худ").getValue()) targetHud.render(e);
        if (elements.is("Ватермарка").getValue()) watermark.render(e);
        if (elements.is("Броня").getValue()) armorHud.render(e);
        if (elements.is("Таймер").getValue()) timerHud.render(e);
        if (elements.is("Задержка предметов").getValue()) cooldowns.render(e);
        if (HynixMain.getInstance().getModuleManager().getNotifications().isEnabled()) notification.render(e);
    }

    public Hud() {
        watermark = new Watermark();
        arrayList = new ArrayList();
        Dragging potions = HynixMain.getInstance().createDrag(this, "Potions", 130, 28);
        armorHud = new ArmorHud();
        Dragging timerInfo = HynixMain.getInstance().createDrag(this, "Timer", 300, 120);
        Dragging keyBinds = HynixMain.getInstance().createDrag(this, "KeyBinds", 58, 28);
        Dragging dragging = HynixMain.getInstance().createDrag(this, "TargetHUD", 116, 94);
        Dragging staffList = HynixMain.getInstance().createDrag(this, "StaffList", 2, 28);
        Dragging schedules = HynixMain.getInstance().createDrag(this, "Schedules", 2, 52);
        Dragging cooldownsdrag = HynixMain.getInstance().createDrag(this, "Cooldowns", 2, 82);
        Dragging notificationsdrag = HynixMain.getInstance().createDrag(this, "Notifications", 165, 5);
        notification = new NotificationRender(notificationsdrag);
        potionHud = new PotionHud(potions);
        keybinds = new Keybinds(keyBinds);
        staffHud = new StaffHud(staffList);
        targetHud = new TargetHud(dragging);
        timerHud = new TimerHud(timerInfo);
        clientInfo = new ClientInfo();
        cooldowns = new Cooldowns(cooldownsdrag);

        this.schedules = new Schedules(schedules);
        addSettings(elements, particlesOnTarget,fixhp, themeMode, theme, themeColor);
    }

    public static int getColor(int firstColor, int secondColor, int index, float mult) {
        return ColorUtils.gradient(firstColor, secondColor, (int) (index * mult), 10);
    }
}