package io.hynix.utils.johon0.animations;


import com.mojang.blaze3d.platform.GlStateManager;
import io.hynix.utils.johon0.math.TimerUtils;

public abstract class AnimationUtils {

    public TimerUtils timerUtil = new TimerUtils();
    protected int duration;
    protected double endPoint;
    protected Direction direction;

    public AnimationUtils(int ms, double endPoint) {
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = Direction.FORWARDS;
    }


    public AnimationUtils(int ms, double endPoint, Direction direction) {
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = direction;
    }

    public boolean finished(Direction direction) {
        return isDone() && this.direction.equals(direction);
    }

    public double getLinearOutput() {
        return 1 - ((timerUtil.getTime() / (double) duration) * endPoint);
    }

    public double getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(double endPoint) {
        this.endPoint = endPoint;
    }

    public void reset() {
        timerUtil.reset();
    }

    public boolean isDone() {
        return timerUtil.isReached(duration);
    }

    public void changeDirection() {
        setDirection(direction.opposite());
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            timerUtil.setTime(System.currentTimeMillis() - (duration - Math.min(duration, timerUtil.getTime())));
        }
    }
    public static void sizeAnimation(double width, double height, double scale) {
         GlStateManager.translated(width, height, 0);
         GlStateManager.scaled(scale, scale, scale);
         GlStateManager.translated(-width, -height, 0);
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }

    protected boolean correctOutput() {
        return false;
    }

    public double getOutput() {
        if (direction == Direction.FORWARDS) {
            if (isDone())
                return endPoint;
            return (getEquation(timerUtil.getTime()) * endPoint);
        } else {
            if (isDone()) return 0;
            if (correctOutput()) {
                double revTime = Math.min(duration, Math.max(0, duration - timerUtil.getTime()));
                return getEquation(revTime) * endPoint;
            } else return (1 - getEquation(timerUtil.getTime())) * endPoint;
        }
    }

    protected abstract double getEquation(double x);

}
