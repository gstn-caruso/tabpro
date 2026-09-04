package com.gstncaruso.tabpro.core.model;

public record Duration(NoteValue value, boolean dotted) {

    public static final int TICKS_PER_QUARTER = 960;

    public static Duration quarter() {
        return new Duration(NoteValue.QUARTER, false);
    }

    public long ticks() {
        long base = TICKS_PER_QUARTER * 4L / value.denominator();
        return dotted ? base * 3 / 2 : base;
    }

    public Duration longer() {
        return new Duration(value.longer(), dotted);
    }

    public Duration shorter() {
        return new Duration(value.shorter(), dotted);
    }

    public Duration toggledDot() {
        return new Duration(value, !dotted);
    }
}
