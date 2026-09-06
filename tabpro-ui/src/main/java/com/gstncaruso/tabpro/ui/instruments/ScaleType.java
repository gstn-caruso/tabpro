package com.gstncaruso.tabpro.ui.instruments;

import java.util.Arrays;
import java.util.Set;

/** Las escalas mas comunes para el modo "beat y escala", como semitonos desde la tonica. */
public enum ScaleType {
    MAJOR("Mayor", 0, 2, 4, 5, 7, 9, 11),
    NATURAL_MINOR("Menor natural", 0, 2, 3, 5, 7, 8, 10),
    MAJOR_PENTATONIC("Pentatonica mayor", 0, 2, 4, 7, 9),
    MINOR_PENTATONIC("Pentatonica menor", 0, 3, 5, 7, 10),
    BLUES("Blues", 0, 3, 5, 6, 7, 10);

    private final String label;
    private final Set<Integer> intervals;

    ScaleType(String label, int... intervals) {
        this.label = label;
        this.intervals = Set.copyOf(Arrays.stream(intervals).boxed().toList());
    }

    public String label() {
        return label;
    }

    boolean has(int semitonesFromRoot) {
        return intervals.contains(Math.floorMod(semitonesFromRoot, 12));
    }

    @Override
    public String toString() {
        return label;
    }
}
