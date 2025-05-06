package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;

import io.hynix.managers.friend.FriendManager;
import io.hynix.events.impl.EventKey;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BindSetting;
import net.minecraft.entity.player.PlayerEntity;

@UnitRegister(name = "ClickFriend", category = Category.Miscellaneous, desc = "Позволяет добавить друга на СКМ")
public class ClickFriend extends Unit {
    final BindSetting throwKey = new BindSetting("Кнопка", -98);
    public ClickFriend() {
        addSettings(throwKey);
    }
    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == throwKey.getValue() && mc.pointedEntity instanceof PlayerEntity) {

            if (mc.player == null || mc.pointedEntity == null) {
                return;
            }

            String playerName = mc.pointedEntity.getName().getString();

            if (FriendManager.isFriend(playerName)) {
                FriendManager.remove(playerName);
                printStatus(playerName, true);
            } else {
                FriendManager.add(playerName);
                printStatus(playerName, false);
            }
        }
    }

    void printStatus(String name, boolean remove) {
        if (remove) print(name + " удалён из друзей");
        else print(name + " добавлен в друзья");
    }
}
