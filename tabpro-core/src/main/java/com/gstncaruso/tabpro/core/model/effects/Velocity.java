package com.gstncaruso.tabpro.core.model.effects;

/** Cuan fuerte suena una nota concreta, ya ajustada por sus efectos. */
public record Velocity(int value) {

    public static final int MIN = 1;
    public static final int MAX = 127;
    private static final int ACCENT_STEP = 16;

    public Velocity {
        value = Math.clamp(value, MIN, MAX);
    }

    public Velocity accented() {
        return new Velocity(value + ACCENT_STEP);
    }

    public Velocity ghosted() {
        return new Velocity(value - ACCENT_STEP);
    }

    public Velocity scaledBy(double factor) {
        return new Velocity((int) Math.round(value * factor));
    }
}
