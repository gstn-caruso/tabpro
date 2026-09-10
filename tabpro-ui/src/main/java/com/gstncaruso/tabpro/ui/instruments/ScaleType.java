package com.gstncaruso.tabpro.ui.instruments;

import java.util.Arrays;
import java.util.Set;

public enum ScaleType {
    MAJOR(0, 2, 4, 5, 7, 9, 11),
    NATURAL_MINOR(0, 2, 3, 5, 7, 8, 10),
    MAJOR_PENTATONIC(0, 2, 4, 7, 9),
    MINOR_PENTATONIC(0, 3, 5, 7, 10),
    BLUES(0, 3, 5, 6, 7, 10);

    private final Set<Integer> intervals;

    ScaleType(int... intervals) {
        this.intervals = Set.copyOf(Arrays.stream(intervals).boxed().toList());
    }

    public Set<Integer> semitones() {
        return intervals;
    }
}
