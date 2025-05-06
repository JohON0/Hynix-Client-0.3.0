package io.hynix.events.impl;

import com.mojang.blaze3d.matrix.MatrixStack;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class EventRender3D {
    private MatrixStack stack;
    private float partialTicks;
    
    public EventRender3D(MatrixStack stack, float partialTicks)
    {
        this.stack = stack;
        this.partialTicks = partialTicks;
    }
    
  
}
