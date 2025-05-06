package io.hynix.units.api;

import io.hynix.HynixMain;
import io.hynix.units.impl.miscellaneous.Sounds;
import io.hynix.ui.notifications.impl.NoNotify;
import io.hynix.ui.notifications.impl.SuccessNotify;
import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.client.SoundPlayer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.List;

import io.hynix.units.settings.api.Setting;
import io.hynix.utils.client.IMinecraft;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public abstract class Unit implements IMinecraft {

    final String name;
    final String desc;
    final Category category;
    final boolean premium;
    boolean enabled;
    @Setter
    int bind;
    final List<Setting<?>> settings = new ObjectArrayList<>();

    final Animation animation = new Animation();

    public Unit() {
        this.name = getClass().getAnnotation(UnitRegister.class).name();
        this.category = getClass().getAnnotation(UnitRegister.class).category();
        this.bind = getClass().getAnnotation(UnitRegister.class).key();
        this.desc = getClass().getAnnotation(UnitRegister.class).desc();
        this.premium = getClass().getAnnotation(UnitRegister.class).premium();
    }

    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(List.of(settings));
    }

    public void betterComp(Unit module) {
        if (getName().equals(module.getName())) {
            return;
        }

        if (module.isEnabled()) {
            module.setEnabled(false, false);
            module.setEnabled(true, false);
            print(getName() + ": улучшил совместимость с модулем " + module.getName());
        }
    }

    public void onEnable() {
        animation.animate(1, 0.25f, Easings.CIRC_OUT);
        HynixMain.getInstance().getEventBus().register(this);

    }

    public void onDisable() {
        animation.animate(0, 0.25f, Easings.CIRC_OUT);
        HynixMain.getInstance().getEventBus().unregister(this);
    }


    public void toggle() {
        setEnabled(!enabled, false);
    }

    public final void setEnabled(boolean newState, boolean config) {
        if (enabled == newState) {
            return;
        }

        enabled = newState;

        try {
            if (enabled) {
                onEnable();
                if (!name.equals("ClickGui")) HynixMain.getInstance().getNotifyManager().add(0, new SuccessNotify(this.name + " | " + TextFormatting.GREEN + "enabled", 1000));
            } else {
                onDisable();
                if (!name.equals("ClickGui")) HynixMain.getInstance().getNotifyManager().add(0, new NoNotify(this.name + " | " + TextFormatting.RED + "disabled", 1000));
            }
            if (!config) {
                UnitManager moduleManager = HynixMain.getInstance().getModuleManager();
                Sounds clientTune = moduleManager.getClientTune();

                if (clientTune != null && clientTune.isEnabled() && !name.equals("ClickGui")) {
                    String fileName = clientTune.getFileName(enabled);
                    float volume = clientTune.volume.getValue();
                    SoundPlayer.playSound(fileName, volume, false);
                }
            }
        } catch (Exception e) {
            handleException(enabled ? "onEnable" : "onDisable", e);
        }

    }

    private void handleException(String methodName, Exception e) {
        if (mc.player != null) {
            print("[" + name + "] Произошла ошибка в методе " + TextFormatting.RED + methodName + TextFormatting.WHITE
                    + "() Предоставьте это сообщение разработчику (@joh0n0 / .report <message>): " + TextFormatting.GRAY + e.getMessage());
            e.printStackTrace();
        } else {
            System.out.println("[" + name + " Error" + methodName + "() Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
