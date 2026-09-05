package com.gstncaruso.tabpro.core.editing.wizards;

/** El rango de compases sobre el que trabaja un asistente, contado desde 1. */
public record MeasureRange(int from, int to) {

    public MeasureRange {
        if (from < 1) {
            throw new IllegalArgumentException("el primer compas se cuenta desde 1: " + from);
        }
        if (to < from) {
            throw new IllegalArgumentException("el rango termina antes de empezar: " + from + ".." + to);
        }
    }

    public static MeasureRange wholeScore(int measureCount) {
        return new MeasureRange(1, Math.max(1, measureCount));
    }

    public static MeasureRange onlyMeasure(int measure) {
        return new MeasureRange(measure, measure);
    }

    public boolean covers(int measureIndex) {
        return measureIndex >= from - 1 && measureIndex <= to - 1;
    }
}
