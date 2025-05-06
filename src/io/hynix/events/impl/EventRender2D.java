package io.hynix.events.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRender2D {
    MatrixStack matrixStack;
    float partialTicks;
    Type type;

    public EventRender2D(MatrixStack matrixStack, float partialTicks) {
        this.matrixStack = matrixStack;
        this.partialTicks = partialTicks;
    }

    public enum Type {
        PRE, POST, HIGH
    }
}
