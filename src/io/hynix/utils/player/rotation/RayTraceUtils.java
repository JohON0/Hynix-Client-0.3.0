package io.hynix.utils.player.rotation;

import io.hynix.utils.client.IMinecraft;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import static io.hynix.utils.player.MouseUtils.getVectorForRotation;


@UtilityClass
public class RayTraceUtils implements IMinecraft {

    public boolean rayTraceSingleEntity(float yaw, float pitch, double distance, Entity entity) {
        Vector3d eyeVec = mc.player.getEyePosition(1.0F);
        Vector3d lookVec = mc.player.getVectorForRotation(pitch, yaw);
        Vector3d extendedVec = eyeVec.add(lookVec.scale(distance));

        AxisAlignedBB AABB = entity.getBoundingBox();

        return AABB.contains(eyeVec) || AABB.rayTrace(eyeVec, extendedVec).isPresent();
    }

    public static RayTraceResult rayTrace(double rayTraceDistance,
                                          float yaw,
                                          float pitch,
                                          Entity entity) {
        Vector3d startVec = mc.player.getEyePosition(1.0F);
        Vector3d directionVec = getVectorForRotation(pitch, yaw);
        Vector3d endVec = startVec.add(
                directionVec.x * rayTraceDistance,
                directionVec.y * rayTraceDistance,
                directionVec.z * rayTraceDistance
        );

        return mc.world.rayTraceBlocks(new RayTraceContext(
                startVec,
                endVec,
                RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE,
                entity)
        );
    }

    public boolean isHitBoxNotVisible(final Vector3d vec3d) {
        final RayTraceContext rayTraceContext = new RayTraceContext(
                mc.player.getEyePosition(1F),
                vec3d,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                mc.player
        );
        final BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);
        return blockHitResult.getType() == RayTraceResult.Type.MISS;
    }
}
