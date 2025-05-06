package io.hynix.utils.player.rotation;

import io.hynix.HynixMain;
import io.hynix.utils.client.IMinecraft;
import io.hynix.utils.johon0.math.VectorUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.joml.Math;
import org.joml.Vector2f;

import static java.lang.Math.PI;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class RotationUtils implements IMinecraft {

    public RotationUtils() {
        HynixMain.getInstance().getEventBus().register(this);
    }

    public Vector3d getClosestVec(Entity entity) {
        Vector3d eyePosVec = mc.player.getEyePosition(1.0F);

        return VectorUtils.getClosestVec(eyePosVec, entity).subtract(eyePosVec);
    }

    public Vector2f calculate(final double x, final double y, final double z) {
        net.minecraft.util.math.vector.Vector3d pos = mc.player.getPositionVec().add(0, mc.player.getEyeHeight(), 0);
        return calculate(new org.joml.Vector3d(pos.x, pos.y, pos.z), new org.joml.Vector3d(x, y, z));
    }

    public static Vector2f calculate(final org.joml.Vector3d to) {
        net.minecraft.util.math.vector.Vector3d pos = mc.player.getPositionVec().add(0, mc.player.getEyeHeight(), 0);
        org.joml.Vector3d from = new org.joml.Vector3d(pos.x, pos.y, pos.z);
        return calculate(from, to);
    }

    public static Vector2f calculate(final org.joml.Vector3d from, final org.joml.Vector3d to) {
        org.joml.Vector3d diff = to.sub(from);
        double distance = java.lang.Math.hypot(diff.x(), diff.z());
        float yaw = (float) (MathHelper.atan2(diff.z(), diff.x()) * 180F / PI) - 90F;
        float pitch = (float) (-(MathHelper.atan2(diff.y(), distance) * 180F / PI));
        yaw = normalize(yaw);
        pitch = (float) Math.clamp(-90, 90, pitch);
        return new Vector2f(yaw, pitch);
    }

    public static float normalize(float value) {
        value = value % 360.0f;
        if (value > 180.0f) {
            value -= 360.0f;
        } else if (value < -180.0f) {
            value += 360.0f;
        }
        return value;
    }

    public static Vector2f calculate(final Entity entity) {
        net.minecraft.util.math.vector.Vector3d pos = entity.getPositionVec().add(0,
                java.lang.Math.max(0,
                        Math.min(mc.player.getPosY() - entity.getPosY() + mc.player.getEyeHeight(),
                                (entity.getBoundingBox().maxY - entity.getBoundingBox().minY) * 0.75F)),
                0);
        org.joml.Vector3d to = new org.joml.Vector3d(pos.x, pos.y, pos.z);
        return calculate(to);
    }

    public double getStrictDistance(Entity entity) {
        return getClosestVec(entity).length();
    }

    public static float[] getMatrixRots(Entity target) {
        double dX = target.getPosX() - mc.player.getPosX();
        double dZ = target.getPosZ() - mc.player.getPosZ();
        double dY = (target.getPosY() + target.getEyeHeight()) - (mc.player.getPosY() + mc.player.getEyeHeight());

        double dist = java.lang.Math.sqrt(dX * dX + dZ * dZ);
        float yaw = (float) (java.lang.Math.toDegrees(java.lang.Math.atan2(dZ, dX)) - 90.0F);
        float pitch = (float) -java.lang.Math.toDegrees(java.lang.Math.atan2(dY, dist));

        return new float[]{yaw, pitch};
    }

}
