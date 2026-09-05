package com.gstncaruso.tabpro.core.model;

public record Duration(NoteValue value, boolean dotted, Tuplet tuplet) {

    public static final int TICKS_PER_QUARTER = 960;

    public Duration(NoteValue value, boolean dotted) {
        this(value, dotted, Tuplet.none());
    }

    public static Duration quarter() {
        return new Duration(NoteValue.QUARTER, false);
    }

    public static Duration of(NoteValue value) {
        return new Duration(value, false);
    }

    public long ticks() {
        return tuplet.apply(dottedTicks());
    }

    private long dottedTicks() {
        long base = TICKS_PER_QUARTER * 4L / value.denominator();
        return dotted ? base * 3 / 2 : base;
    }

    public Duration longer() {
        return new Duration(value.longer(), dotted, tuplet);
    }

    public Duration shorter() {
        return new Duration(value.shorter(), dotted, tuplet);
    }

    public Duration toggledDot() {
        return new Duration(value, !dotted, tuplet);
    }

    public Duration in(Tuplet tuplet) {
        return new Duration(value, dotted, tuplet);
    }
}
