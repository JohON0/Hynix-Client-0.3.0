package io.hynix.units.impl.display;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.EventCancelOverlay;
import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.ModeListSetting;
import net.minecraft.potion.Effects;

@UnitRegister(name = "Remover", category = Category.Display,desc = "Удаляет не нужные элементы игры")
public class Remover extends Unit {

    public ModeListSetting element = new ModeListSetting("Удалять",
            new BooleanSetting("Огонь на экране", true),
            new BooleanSetting("Линия босса", false),
            new BooleanSetting("Анимация тотема", true),
            new BooleanSetting("Тайтлы", false),
            new BooleanSetting("Таблица", false),
            new BooleanSetting("Туман", true),
            new BooleanSetting("Тряску камеры", true),
            new BooleanSetting("Плохие эффекты", true),
            new BooleanSetting("Дождь", true),
            new BooleanSetting("Камера клип", true),
            new BooleanSetting("Броня", false),
            new BooleanSetting("Плащ", false),
            new BooleanSetting("Эффект свечения", true),
            new BooleanSetting("Эффект воды", true),
            new BooleanSetting("Трава", true),
            new BooleanSetting("Партиклы", false)
    );

    public Remover() {
        addSettings(element);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        handleEventUpdate(e);
    }

    @Subscribe
    private void onEventCancelOverlay(EventCancelOverlay e) {
        handleEventOverlaysRender(e);
    }

    private void handleEventOverlaysRender(EventCancelOverlay event) {
        boolean cancelOverlay = switch (event.overlayType) {
            case FIRE_OVERLAY -> element.is("Огонь на экране").getValue();
            case BOSS_LINE -> element.is("Линия босса").getValue();
            case SCOREBOARD -> element.is("Таблица").getValue();
            case TITLES -> element.is("Тайтлы").getValue();
            case TOTEM -> element.is("Анимация тотема").getValue();
            case FOG -> element.is("Туман").getValue();
            case HURT -> element.is("Тряску камеры").getValue();
            case UNDER_WATER -> element.is("Эффект воды").getValue();
            case CAMERA_CLIP -> element.is("Камера клип").getValue();
            case ARMOR -> element.is("Броня").getValue();
        };

        if (cancelOverlay) {
            event.cancel();
        }
    }

    private void handleEventUpdate(EventUpdate event) {
        boolean isRaining = mc.world.isRaining() && element.is("Дождь").getValue();

        boolean hasEffects = (mc.player.isPotionActive(Effects.BLINDNESS)
                || mc.player.isPotionActive(Effects.NAUSEA)) && element.is("Плохие эффекты").getValue();

        if (isRaining) {
            mc.world.setRainStrength(0);
            mc.world.setThunderStrength(0);
        }

        if (hasEffects) {
            mc.player.removePotionEffect(Effects.NAUSEA);
            mc.player.removePotionEffect(Effects.BLINDNESS);
        }
    }
}
