package io.hynix.ui.clickgui.components.builder;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IBuilder {

    default void render(MatrixStack stack, float mouseX, float mouseY) {
    }

    default boolean mouseClick(float mouseX, float mouseY, int mouse) {
        return false;
    }
    default void charTyped(char codePoint, int modifiers) {
    }
    default void mouseRelease(float mouseX, float mouseY, int mouse) {
    }
    default void keyPressed(int key, int scanCode, int modifiers) {
    }
}
