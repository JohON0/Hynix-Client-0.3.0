package io.hynix.units.impl.miscellaneous;

import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import net.minecraft.client.gui.screen.DeathScreen;
/**
 * @author JohON0
 */
@UnitRegister(name = "DeathCoords", category = Category.Miscellaneous, desc = "Отправляет в чат коодинаты смерти")
public class DeathCoords extends Unit {
    final BooleanSetting waypoint = new BooleanSetting("Создавать точку", false);

    @Override
    public void onEnable() {
        if (mc.currentScreen instanceof DeathScreen) {
            print("Координаты смерти: " + (int) mc.player.getPosX() + ", " + (int) mc.player.getPosY() + ", " + (int) mc.player.getPosZ());
            if (waypoint.getValue()) {
                mc.player.sendChatMessage(".way add Точка Смерти " + (int) mc.player.getPosX() + " " + (int) mc.player.getPosY() + " " + (int) mc.player.getPosZ());
            }
        }
        super.onEnable();
    }

    public void onDisable() {
        //konfetka pidaras
        super.onDisable();
    }

}
