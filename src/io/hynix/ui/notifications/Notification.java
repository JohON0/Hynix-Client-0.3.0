package io.hynix.ui.notifications;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.hynix.utils.johon0.animations.easing.CompactAnimation;
import io.hynix.utils.johon0.animations.easing.Easing;
import io.hynix.utils.client.IMinecraft;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class Notification implements IMinecraft {
    public final String content;
    public final long init = System.currentTimeMillis(), delay;
    public final CompactAnimation alphaAnimation = new CompactAnimation(Easing.EASE_OUT_EXPO, 250);
    public final CompactAnimation animationY = new CompactAnimation(Easing.EASE_OUT_EXPO, 250);
    public final CompactAnimation animationX = new CompactAnimation(Easing.EASE_OUT_BACK, 250);
    public final CompactAnimation chatOffset = new CompactAnimation(Easing.EASE_OUT_QUAD, 50);
    public static boolean end;
    public float margin = 4;

    public abstract void render(MatrixStack matrixStack, int multiplierY);

    public abstract boolean hasExpired();
}
