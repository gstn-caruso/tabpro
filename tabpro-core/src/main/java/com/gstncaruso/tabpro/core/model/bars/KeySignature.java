package com.gstncaruso.tabpro.core.model.bars;

import java.util.List;

public record KeySignature(int accidentals, Mode mode) {

    private static final List<Integer> SHARP_ORDER = List.of(3, 0, 4, 1, 5, 2, 6);

    private static final List<Integer> FLAT_ORDER = List.of(6, 2, 5, 1, 4, 0, 3);

    public KeySignature {
        if (accidentals < -7 || accidentals > 7) {
            throw new IllegalArgumentException("the key signature ranges from -7 to 7 accidentals: " + accidentals);
        }
    }

    public static KeySignature cMajor() {
        return new KeySignature(0, Mode.MAJOR);
    }

    public boolean hasSharps() {
        return accidentals > 0;
    }

    public boolean hasFlats() {
        return accidentals < 0;
    }

    public int alteredCount() {
        return Math.abs(accidentals);
    }

    public List<Integer> alteredSteps() {
        return (hasFlats() ? FLAT_ORDER : SHARP_ORDER).subList(0, alteredCount());
    }

    public int alterationOf(int step) {
        if (!alteredSteps().contains(step)) {
            return 0;
        }
        return hasFlats() ? -1 : 1;
    }
}
