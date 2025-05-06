package io.hynix.utils.johon0.animations.impl;


import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;

public class DecelerateAnimation extends AnimationUtils {

    public DecelerateAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public DecelerateAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    protected double getEquation(double x) {
        double x1 = x / duration;
        return 1 - ((x1 - 1) * (x1 - 1));
    }
}
