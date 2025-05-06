package io.hynix.units.impl.combat;

import com.google.common.eventbus.Subscribe;

import io.hynix.events.impl.EventUpdate;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import io.hynix.units.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
/**
 * @author JohON0
 */
@UnitRegister(name = "HitBox", category = Category.Combat,desc = "Увеличивает хитбокс игрока")
public class HitBox extends Unit {
    public final SliderSetting size = new SliderSetting("Размер", 0.2f, 0.f, 3.f, 0.05f);
    public final BooleanSetting visible = new BooleanSetting("Видимые", false);
    public HitBox() {
        addSettings(size,visible);
    }
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (!visible.getValue() || mc.player == null) {
            return;
        }

        float sizeMultiplier = this.size.getValue() * 2.5F;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!isNotValid(player)) {
                player.setBoundingBox(calculateBoundingBox(player, sizeMultiplier));
            }
        }
    }

    private boolean isNotValid(PlayerEntity player) {
        return player == mc.player || !player.isAlive();
    }

    private AxisAlignedBB calculateBoundingBox(Entity entity, float size) {
        double minX = entity.getPosX() - size;
        double minY = entity.getBoundingBox().minY;
        double minZ = entity.getPosZ() - size;
        double maxX = entity.getPosX() + size;
        double maxY = entity.getBoundingBox().maxY;
        double maxZ = entity.getPosZ() + size;

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
