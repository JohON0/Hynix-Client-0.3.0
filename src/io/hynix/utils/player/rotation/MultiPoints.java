package io.hynix.utils.player.rotation;

import io.hynix.utils.client.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
//import org.joml.Vector3d;

public class MultiPoints implements IMinecraft {

    public static Vector3d getBestPoint(Entity entity) {
        float x = (float) limit(entity.getBoundingBox().minX, entity.getBoundingBox().maxX - entity.getBoundingBox().minX, mc.player.getPosX());
        float y = (float) limit(entity.getBoundingBox().minY, entity.getBoundingBox().maxY - entity.getBoundingBox().minY, mc.player.getPosY() + mc.player.getEyeHeight());
        float z = (float) limit(entity.getBoundingBox().minZ, entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ, mc.player.getPosZ());

        return new Vector3d(
                entity.getBoundingBox().minX + (entity.getBoundingBox().maxX - entity.getBoundingBox().minX) * x,
                entity.getBoundingBox().minY + (entity.getBoundingBox().maxY - entity.getBoundingBox().minY) * y,
                entity.getBoundingBox().minZ + (entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ) * z
        );
    }

    private static double limit(double min, double max, double current) {
        return Math.min(1, Math.max(0, (current - min) / max));
    }

    public static Vector3d calculateVector(Entity entity, boolean useBoundingBox) {
        if (useBoundingBox) {
            double x = (entity.getBoundingBox().minX + entity.getBoundingBox().maxX) / 2;
            double y = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2;
            double z = (entity.getBoundingBox().minZ + entity.getBoundingBox().maxZ) / 2;

            return new Vector3d(
                    x - mc.player.getPosX(),
                    y - (mc.player.getPosY() + mc.player.getEyeHeight()),
                    z - mc.player.getPosZ()
            );
        } else {
            // Используем позицию сущности
            double wHalf = entity.getWidth() / 2;
            double yExpand = clamp(entity.getPosYEye() - entity.getPosY(), 0, entity.getHeight());
            double xExpand = clamp(mc.player.getPosX() - entity.getPosX(), -wHalf, wHalf);
            double zExpand = clamp(mc.player.getPosZ() - entity.getPosZ(), -wHalf, wHalf);

            return new Vector3d(
                    entity.getPosX() - mc.player.getPosX() + xExpand,
                    entity.getPosY() - mc.player.getPosYEye() + yExpand,
                    entity.getPosZ() - mc.player.getPosZ() + zExpand
            );
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

}
