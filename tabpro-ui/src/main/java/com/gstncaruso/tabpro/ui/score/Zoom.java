package com.gstncaruso.tabpro.ui.score;

import java.util.List;

/** El zoom de la partitura, del 30% al 200% como pide el manual. */
public record Zoom(int percent) {

    public static final int MIN_PERCENT = 30;
    public static final int MAX_PERCENT = 200;
    private static final int STEP = 10;
    private static final List<Integer> PRESETS = List.of(30, 50, 75, 100, 125, 150, 175, 200);

    public Zoom {
        percent = Math.clamp(percent, MIN_PERCENT, MAX_PERCENT);
    }

    public static Zoom whole() {
        return new Zoom(100);
    }

    public static List<Integer> presets() {
        return PRESETS;
    }

    public double factor() {
        return percent / 100.0;
    }

    public Zoom in() {
        return new Zoom(percent + STEP);
    }

    public Zoom out() {
        return new Zoom(percent - STEP);
    }

    public boolean isAtMinimum() {
        return percent == MIN_PERCENT;
    }

    public boolean isAtMaximum() {
        return percent == MAX_PERCENT;
    }
}
