package io.hynix.utils.player.rotation;

import com.google.common.eventbus.Subscribe;
import io.hynix.HynixMain;
import io.hynix.events.impl.EventUpdate;
import io.hynix.utils.client.IMinecraft;
import io.hynix.utils.johon0.math.SensUtils;

import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class RotationHandler implements IMinecraft {
    public RotationHandler() {
        HynixMain.getInstance().getEventBus().register(this);
    }

    private static RotationTask currentTask = RotationTask.IDLE;

    private static float currentTurnSpeed;
    private static int currentPriority;
    private static int currentTimeout;

    private static int idleTicks;

    @Subscribe
    public void onUpdate(EventUpdate e) {
        idleTicks++;

        if (currentTask == RotationTask.AIM && idleTicks > currentTimeout) {
            currentTask = RotationTask.RESET;
        }

        if (currentTask == RotationTask.RESET) {
            if (updateRotation(Rotation.getReal(), currentTurnSpeed)) {
                currentTask = RotationTask.IDLE;
                currentPriority = 0;
                FreeLookHandler.setActive(false);
            }
        }
    }

    public static void update(Rotation rotation, float turnSpeed, int timeout, int priority) {
        if (currentPriority > priority) {
            return;
        }

        if (currentTask == RotationTask.IDLE) {
            FreeLookHandler.setActive(true);
        }

        currentTurnSpeed = turnSpeed;
        currentTimeout = timeout;
        currentPriority = priority;

        currentTask = RotationTask.AIM;

        updateRotation(rotation, turnSpeed);
    }

    private static boolean updateRotation(Rotation rotation, float turnSpeed) {
        Rotation currentRotation = new Rotation(mc.player);

        float yawDelta = wrapDegrees(rotation.getYaw() - currentRotation.getYaw());
        float pitchDelta = rotation.getPitch() - currentRotation.getPitch();

        float totalDelta = Math.abs(yawDelta) + Math.abs(pitchDelta);

        float yawSpeed = (totalDelta == 0) ? 0 : Math.abs(yawDelta / totalDelta) * turnSpeed;
        float pitchSpeed = (totalDelta == 0) ? 0 : Math.abs(pitchDelta / totalDelta) * turnSpeed;

        mc.player.rotationYaw += SensUtils.getSensitivity(clamp(yawDelta, -yawSpeed, yawSpeed));
        mc.player.rotationPitch = clamp(mc.player.rotationPitch + SensUtils.getSensitivity(clamp(pitchDelta, -pitchSpeed, pitchSpeed)), -90, 90);

        Rotation finalRotation = new Rotation(mc.player);

        idleTicks = 0;

        return finalRotation.getDelta(rotation) < currentTurnSpeed;
    }

    private enum RotationTask {
        AIM,
        RESET,
        IDLE
    }
}
