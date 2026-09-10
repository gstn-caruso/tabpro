package com.gstncaruso.tabpro.core.harmony;

import java.util.Arrays;
import java.util.Optional;

public enum Interval {
    ROOT(0, 0, "1"),
    AUGMENTED_UNISON(0, 1, "#1"),
    MINOR_SECOND(1, 1, "b2"),
    MAJOR_SECOND(1, 2, "2"),
    AUGMENTED_SECOND(1, 3, "#2"),
    MINOR_THIRD(2, 3, "b3"),
    MAJOR_THIRD(2, 4, "3"),
    DIMINISHED_FOURTH(3, 4, "b4"),
    PERFECT_FOURTH(3, 5, "4"),
    AUGMENTED_FOURTH(3, 6, "#4"),
    DIMINISHED_FIFTH(4, 6, "b5"),
    PERFECT_FIFTH(4, 7, "5"),
    AUGMENTED_FIFTH(4, 8, "#5"),
    MINOR_SIXTH(5, 8, "b6"),
    MAJOR_SIXTH(5, 9, "6"),
    AUGMENTED_SIXTH(5, 10, "#6"),
    DIMINISHED_SEVENTH(6, 9, "bb7"),
    MINOR_SEVENTH(6, 10, "b7"),
    MAJOR_SEVENTH(6, 11, "7"),
    MINOR_NINTH(1, 13, "b9"),
    MAJOR_NINTH(1, 14, "9"),
    AUGMENTED_NINTH(1, 15, "#9"),
    PERFECT_ELEVENTH(3, 17, "11"),
    AUGMENTED_ELEVENTH(3, 18, "#11"),
    MINOR_THIRTEENTH(5, 20, "b13"),
    MAJOR_THIRTEENTH(5, 21, "13");

    private final int letterSteps;
    private final int semitones;
    private final String label;

    Interval(int letterSteps, int semitones, String label) {
        this.letterSteps = letterSteps;
        this.semitones = semitones;
        this.label = label;
    }

    public int letterSteps() {
        return letterSteps;
    }

    public int semitones() {
        return semitones;
    }

    public String label() {
        return label;
    }

    public int degreeNumber() {
        return letterSteps + 1 + (semitones >= 12 ? 7 : 0);
    }

    public PitchClass from(PitchClass root) {
        return root.steppedBy(letterSteps, semitones);
    }

    public static Optional<Interval> matching(int letterSteps, int semitones) {
        int steps = Math.floorMod(letterSteps, 7);
        int pitchClass = Math.floorMod(semitones, 12);
        return Arrays.stream(values())
                .filter(interval -> interval.letterSteps == steps && interval.semitones % 12 == pitchClass)
                .findFirst();
    }
}
