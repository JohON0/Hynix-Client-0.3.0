package io.hynix.utils.johon0.math;

import io.hynix.utils.client.IMinecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static net.minecraft.client.renderer.WorldRenderer.frustum;

public class PlayerPositionTracker implements IMinecraft {
    private final static IntBuffer viewport;
    private final static FloatBuffer modelview;
    private final static FloatBuffer projection;
    private final static FloatBuffer vector;

    static {
        viewport = GLAllocation.createDirectIntBuffer(16);
        modelview = GLAllocation.createDirectFloatBuffer(16);
        projection = GLAllocation.createDirectFloatBuffer(16);
        vector = GLAllocation.createDirectFloatBuffer(4);
    }


    public static boolean isInView(Entity ent) {
        assert mc.getRenderViewEntity() != null;
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y,mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(ent.getBoundingBox()) || ent.ignoreFrustumCheck;
    }

    public static boolean isInView(Vector3d ent) {
        assert mc.getRenderViewEntity() != null;
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y,mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(new AxisAlignedBB(ent.add(-0.5,-0.5, -0.5), ent.add(0.5,0.5, 0.5)));
    }
}

