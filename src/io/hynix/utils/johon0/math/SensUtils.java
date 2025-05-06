package io.hynix.utils.johon0.math;

import io.hynix.utils.client.IMinecraft;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;

@UtilityClass
public class SensUtils implements IMinecraft {
    public static float getSensitivity(float rot) {
        return getDeltaMouse(rot) * getGCDValue();
    }

    public static float getGCDValue() {
        return (float) (getGCD() * 0.15);
    }

    public static float getGCD() {
        float f1;
        return (f1 = (float) (mc.gameSettings.mouseSensitivity * 0.6 + 0.2)) * f1 * f1 * 8;
    }
    public static float smoothGCD(float currentAngle, float targetAngle, float maxDelta) {
        float deltaAngle = MathHelper.wrapDegrees(targetAngle - currentAngle);
        float gcd = getGCDValue();
        deltaAngle = MathHelper.clamp(deltaAngle, -maxDelta, maxDelta);
        deltaAngle -= deltaAngle % gcd;
        return currentAngle + deltaAngle;
    }
    public static float getDeltaMouse(float delta) {
        return Math.round(delta / getGCDValue());
    }

}