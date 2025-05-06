package io.hynix.units.impl.traversal;


import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventTick;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.utils.player.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.Items;

@UnitRegister(
        name = "AntiTarget",
        desc = "Антитаргет воздуха на элитре",
        category = Category.Traversal
)
public class AntiTarget extends Unit {
    private static float lockedPitch;
    public static boolean isLocked;
    private long lastFireworkTime = 0L;
    private long fireworkCooldown = 750L;

    public AntiTarget() {
    }

    public void onEnable() {
        super.onEnable();
        isLocked = true;
    }

    public static void lockPitch(float pitch) {
        lockedPitch = pitch;

        float yaw;
        for(yaw = Minecraft.getInstance().player.rotationYaw; yaw < 0.0F; yaw += 360.0F) {
        }

        while(yaw > 360.0F) {
            yaw -= 360.0F;
        }

        isLocked = true;
    }

    public static void unlockPitch() {
        isLocked = false;
    }

    @Subscribe
    public void onUpdate(EventTick tick) {
        if (isLocked) {
            if (mc.world != null) {
                ClientPlayerEntity player = mc.player;
                if (player != null) {
                    lockPitch(-45.0F);
                    float curPitch = player.rotationPitch;
                    float delta = lockedPitch - curPitch;
                    if (delta > 5.0F) {
                        delta = 5.0F;
                    }

                    if (delta < -5.0F) {
                        delta = -5.0F;
                    }

                    player.rotationPitch += delta;
                    if (mc.player.isElytraFlying()) {
                        this.useFireWork();
                    }

                }
            }
        }
    }

    private void useFireWork() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastFireworkTime >= this.fireworkCooldown - 250L) {
            InventoryUtils.getInstance().inventorySwapClick(Items.FIREWORK_ROCKET, false);
            this.lastFireworkTime = currentTime;
        }

    }

    public void onDisable() {
        super.onDisable();
        isLocked = false;
    }
}
