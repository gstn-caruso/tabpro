package com.gstncaruso.tabpro.core.playback;

import java.util.ArrayList;
import java.util.List;

/**
 * El rango de compases de un loop de practica: se toca una y otra vez para
 * automatizar un pasaje dificil.
 */
public record LoopRange(int fromMeasure, int toMeasure) {

    public LoopRange {
        if (fromMeasure < 0) {
            throw new IllegalArgumentException("fromMeasure debe ser >= 0: " + fromMeasure);
        }
        if (toMeasure < fromMeasure) {
            throw new IllegalArgumentException("toMeasure no puede ser anterior a fromMeasure");
        }
    }

    public boolean contains(int measureIndex) {
        return measureIndex >= fromMeasure && measureIndex <= toMeasure;
    }

    public int measureCount() {
        return toMeasure - fromMeasure + 1;
    }

    /** El orden de reproduccion de repetir este rango esa cantidad de vueltas. */
    public PlayOrder asPlayOrder(int laps) {
        List<Integer> sequence = new ArrayList<>();
        for (int lap = 0; lap < laps; lap++) {
            for (int measure = fromMeasure; measure <= toMeasure; measure++) {
                sequence.add(measure);
            }
        }
        return new PlayOrder(sequence);
    }
}
