package io.hynix.events.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.vector.Matrix4f;

@Getter
@AllArgsConstructor
public class EventPreRender3D {
    private final MatrixStack matrix;
    private final Matrix4f projectionMatrix;
    private final ActiveRenderInfo activeRenderInfo;
    private final WorldRenderer context;
    private final float partialTicks;
    private final long finishTimeNano;
    private double cx, cy, cz;
}
