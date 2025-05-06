package io.hynix.utils.player;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import java.util.regex.Pattern;

@UtilityClass
public class PlayerUtils {

    Minecraft mc = Minecraft.getInstance();

    private final Pattern NAME_REGEX = Pattern.compile("^[A-zА-я0-9_]{3,16}$");

    public boolean isNameValid(String name) {
        return NAME_REGEX.matcher(name).matches();
    }


    public static String stringEntityBps(Entity entity, boolean timerCheck) {
        return String.format("%.2f", Math.hypot(entity.prevPosX - entity.getPosX(), entity.prevPosZ - entity.getPosZ()) * 20 * (timerCheck ? mc.timer.timerSpeed : 1));
    }

    public static double getEntityBPS(Entity entity, boolean timerCheck) {
        return Math.hypot(entity.prevPosX - entity.getPosX(), entity.prevPosZ - entity.getPosZ()) * 20 * (timerCheck ? mc.timer.timerSpeed : 1);
    }

    public static float calculateCorrectYawOffset(float yaw) {
        float renderYawOffset;
        double xDiff = mc.player.getPosX() - mc.player.prevPosX;
        double zDiff = mc.player.getPosZ() - mc.player.prevPosZ;
        float distSquared = (float)(xDiff * xDiff + zDiff * zDiff);
        float offset = renderYawOffset = mc.player.prevRenderYawOffset;
        if (distSquared > 0.0025000002f) {
            offset = (float)MathHelper.atan2(zDiff, xDiff) * 180.0f / (float)Math.PI - 90.0f;
        }
        if (mc.player != null && mc.player.swingProgress > 0.0f) {
            offset = yaw;
        }
        float yawOffsetDiff = MathHelper.wrapDegrees(yaw - (renderYawOffset + MathHelper.wrapDegrees(offset - renderYawOffset) * 0.3f));
        yawOffsetDiff = MathHelper.clamp(yawOffsetDiff, -75.0f, 75.0f);
        renderYawOffset = yaw - yawOffsetDiff;
        if (yawOffsetDiff * yawOffsetDiff > 2500.0f) {
            renderYawOffset += yawOffsetDiff * 0.2f;
        }
        return renderYawOffset;
    }
}
