package io.hynix.utils.johon0.math.animation.util;

public enum EnumEasings {
    LINEAR(value -> value),
    CIRC_IN(value -> 1 - Math.sqrt(1 - value * value)),
    CIRC_OUT(value -> Math.sqrt(1 - Math.pow(value - 1, 2))),
    CIRC_IN_OUT(value -> {
        if (value < 0.5) {
            return (1 - Math.sqrt(1 - Math.pow(value * 2, 2))) / 2;
        } else {
            return (Math.sqrt(1 - Math.pow((value - 1) * 2, 2)) + 1) / 2;
        }
    }),
    EXPO_IN(value -> (value == 0) ? 0 : Math.pow(2, 10 * (value - 1))),
    EXPO_OUT(value -> (value == 1) ? 1 : 1 - Math.pow(2, -10 * value)),
    EXPO_IN_OUT(value -> {
        if (value < 0.5) {
            return (Math.pow(2, 10 * (value * 2 - 1)) / 2);
        } else {
            return (2 - Math.pow(2, -10 * (value * 2 - 1))) / 2;
        }
    }),
    BACK_IN(value -> {
        double s = 1.70158; // Параметр для функции Back
        return value * value * ((s + 1) * value - s);
    }),
    BACK_OUT(value -> {
        double s2 = 1.70158; // Параметр для функции Back
        return (value - 1) * (value - 1) * ((s2 + 1) * (value - 1) + s2) + 1;
    }),
    BACK_IN_OUT(value -> {
        double s = 1.70158; // Параметр для функции Back
        if (value < 0.5) {
            return (value * 2) * (value * 2) * ((s + 1) * (value * 2) - s) / 2;
        } else {
            double adjustedValue = (value - 1) * 2;
            return (adjustedValue * adjustedValue * ((s + 1) * adjustedValue + s) + 2) / 2;
        }
    }),
    QUAD_IN(value -> value * value),
    QUAD_OUT(value -> value * (2 - value)),
    QUAD_IN_OUT(value -> {
        if (value < 0.5) {
            return 2 * value * value; // Увеличение
        } else {
            return -1 + (4 - 2 * value) * value; // Уменьшение
        }
    }),
    CUBIC_IN(value -> value * value * value),
    CUBIC_OUT(value -> {
        double f = value - 1;
        return f * f * f + 1; // (t-1)^3 + 1
    }),
    CUBIC_IN_OUT(value -> {
        if (value < 0.5) {
            return 4 * value * value * value; // Увеличение
        } else {
            double f = (value - 1);
            return 4 * f * f * f + 1; // Уменьшение
        }
    }),
    QUART_IN(value -> value * value * value * value),
    QUART_OUT(value -> {
        double f = value - 1;
        return -(f * f * f * f - 1); // -(t-1)^4 + 1
    }),
    QUART_IN_OUT(value -> {
        if (value < 0.5) {
            return 8 * value * value * value * value; // Увеличение
        } else {
            double f = value - 1;
            return -8 * f * f * f * f + 1; // Уменьшение
        }
    }),
    QUINT_IN(value -> value * value * value * value * value),
    QUINT_OUT(value -> {
        double f = value - 1;
        return f * f * f * f * f + 1; // (t-1)^5 + 1
    }),
    QUINT_IN_OUT(value -> {
        if (value < 0.5) {
            return 16 * value * value * value * value * value; // Увеличение
        } else {
            double f = (value - 1);
            return 16 * f * f * f * f * f + 1; // Уменьшение
        }
    }),
    SINE_IN(value -> 1 - Math.cos((value * Math.PI) / 2)),
    SINE_OUT(value -> Math.sin((value * Math.PI) / 2)),
    SINE_IN_OUT(value -> -0.5 * (Math.cos(Math.PI * value) - 1)),
    ELASTIC_IN(value -> {
        double s = 1.70158;
        double p = 0.3; // Параметр для функции Elastic
        if (value == 0 || value == 1) return value;
        double a = 1; // Амплитуда
        double t = p / (2 * Math.PI) * Math.asin(1 / a);
        return -(a * Math.pow(2, 10 * (value -= 1)) * Math.sin((value - t) * (2 * Math.PI) / p));
    }),

    ELASTIC_OUT(value -> {
        double s = 1.70158;
        double p = 0.3; // Параметр для функции Elastic
        if (value == 0 || value == 1) return value;
        double a = 1; // Амплитуда
        double t = p / (2 * Math.PI) * Math.asin(1 / a);
        return a * Math.pow(2, -10 * value) * Math.sin((value - t) * (2 * Math.PI) / p) + 1;
    }),

    ELASTIC_IN_OUT(value -> {
        double s = 1.70158;
        double p = 0.3; // Параметр для функции Elastic
        if (value == 0 || value == 1) return value;
        double a = 1; // Амплитуда
        double t = p / (2 * Math.PI) * Math.asin(1 / a);

        if (value < 0.5) {
            return -0.5 * (a * Math.pow(2, 10 * (value * 2 - 1)) * Math.sin((value * 2 - t) * (2 * Math.PI) / p));
        } else {
            return a * Math.pow(2, -10 * (value * 2 - 1)) * Math.sin((value * 2 - t) * (2 * Math.PI) / p) * 0.5 + 0.5;
        }
    }),
    BOUNCE_OUT(value -> {
        if (value < (1 / 2.75)) {
            return 7.5625 * value * value;
        } else if (value < (2 / 2.75)) {
            value -= (1.5 / 2.75);
            return 7.5625 * value * value + 0.75;
        } else if (value < (2.5 / 2.75)) {
            value -= (2.25 / 2.75);
            return 7.5625 * value * value + 0.9375;
        } else {
            value -= (2.625 / 2.75);
            return 7.5625 * value * value + 0.984375;
        }
    }),

    BOUNCE_IN(value -> {
        return 1 - BOUNCE_OUT.ease(1 - value);
    }),


    BOUNCE_IN_OUT(value -> {
        if (value < 0.5) {
            return BOUNCE_IN.ease(value * 2) * 0.5;
        } else {
            return BOUNCE_OUT.ease(value * 2 - 1) * 0.5 + 0.5;
        }
    });

    private final Easing easingFunction;

    EnumEasings(Easing easingFunction) {
        this.easingFunction = easingFunction;
    }

    public double ease(double value) {
        return easingFunction.ease(value);
    }
}