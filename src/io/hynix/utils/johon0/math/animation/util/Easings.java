package io.hynix.utils.johon0.math.animation.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Easings {
    public final double c1 = 1.70158D;
    public final double c2 = 2.5949095D;
    public final double c3 = 2.70158D;
    public final double c4 = 2.0943951023931953D;
    public final double c5 = 1.3962634015954636D;
    public final Easing LINEAR = EnumEasings.LINEAR::ease;
    public final Easing QUAD_IN = EnumEasings.QUAD_IN::ease;
    public final Easing QUAD_OUT = EnumEasings.QUAD_OUT::ease;
    public final Easing QUAD_IN_OUT = EnumEasings.QUAD_IN_OUT::ease;
    public final Easing CUBIC_IN = EnumEasings.CUBIC_IN::ease;
    public final Easing CUBIC_OUT = EnumEasings.CUBIC_OUT::ease;
    public final Easing CUBIC_IN_OUT = EnumEasings.CUBIC_IN_OUT::ease;
    public final Easing QUART_IN = EnumEasings.QUART_IN::ease;
    public final Easing QUART_OUT = EnumEasings.QUART_OUT::ease;
    public final Easing QUART_IN_OUT = EnumEasings.QUART_IN_OUT::ease;
    public final Easing QUINT_IN = EnumEasings.QUINT_IN::ease;
    public final Easing QUINT_OUT = EnumEasings.QUINT_OUT::ease;
    public final Easing QUINT_IN_OUT = EnumEasings.QUINT_IN_OUT::ease;
    public final Easing SINE_IN = EnumEasings.SINE_IN::ease;
    public final Easing SINE_OUT = EnumEasings.SINE_OUT::ease;
    public final Easing SINE_IN_OUT = EnumEasings.SINE_IN_OUT::ease;
    public final Easing CIRC_IN = EnumEasings.CIRC_IN::ease;
    public final Easing CIRC_OUT = EnumEasings.CIRC_OUT::ease;
    public final Easing CIRC_IN_OUT = EnumEasings.CIRC_IN_OUT::ease;
    public final Easing EXPO_IN = EnumEasings.EXPO_IN::ease;
    public final Easing EXPO_OUT = EnumEasings.EXPO_OUT::ease;
    public final Easing EXPO_IN_OUT = EnumEasings.EXPO_IN_OUT::ease;;
    public final Easing BACK_IN = EnumEasings.BACK_IN::ease;
    public final Easing BACK_OUT = EnumEasings.BACK_OUT::ease;
    public final Easing BACK_IN_OUT = EnumEasings.BACK_IN_OUT::ease;
    public final Easing BOUNCE_IN = EnumEasings.BOUNCE_IN::ease;
    public final Easing BOUNCE_OUT = EnumEasings.BOUNCE_OUT::ease;
    public final Easing BOUNCE_IN_OUT = EnumEasings.BOUNCE_IN_OUT::ease;
    public final Easing ELASTIC_IN = EnumEasings.ELASTIC_IN::ease;
    public final Easing ELASTIC_OUT = EnumEasings.ELASTIC_OUT::ease;
    public final Easing ELASTIC_IN_OUT = EnumEasings.ELASTIC_IN_OUT::ease;

    public Easing powIn(double n) {
        return (value) -> Math.pow(value, n);
    }

    public Easing powIn(int n) {
        return powIn((double) n);
    }

    public Easing powOut(double n) {
        return (value) -> 1.0D - Math.pow(1.0D - value, n);
    }

    public Easing powOut(int n) {
        return powOut((double) n);
    }

    public Easing powIN_OUT(double n) {
        return (value) -> value < 0.5D ? Math.pow(2.0D, n - 1.0D) * Math.pow(value, n) : 1.0D - Math.pow(-2.0D * value + 2.0D, n) / 2.0D;
    }
}