package com.gstncaruso.tabpro.core.harmony;

import java.util.Arrays;
import java.util.Optional;

public enum Interval {
    ROOT(0, 0),
    AUGMENTED_UNISON(0, 1),
    MINOR_SECOND(1, 1),
    MAJOR_SECOND(1, 2),
    AUGMENTED_SECOND(1, 3),
    MINOR_THIRD(2, 3),
    MAJOR_THIRD(2, 4),
    DIMINISHED_FOURTH(3, 4),
    PERFECT_FOURTH(3, 5),
    AUGMENTED_FOURTH(3, 6),
    DIMINISHED_FIFTH(4, 6),
    PERFECT_FIFTH(4, 7),
    AUGMENTED_FIFTH(4, 8),
    MINOR_SIXTH(5, 8),
    MAJOR_SIXTH(5, 9),
    AUGMENTED_SIXTH(5, 10),
    DIMINISHED_SEVENTH(6, 9),
    MINOR_SEVENTH(6, 10),
    MAJOR_SEVENTH(6, 11),
    MINOR_NINTH(1, 13),
    MAJOR_NINTH(1, 14),
    AUGMENTED_NINTH(1, 15),
    PERFECT_ELEVENTH(3, 17),
    AUGMENTED_ELEVENTH(3, 18),
    MINOR_THIRTEENTH(5, 20),
    MAJOR_THIRTEENTH(5, 21);

    private final int letterSteps;
    private final int semitones;

    Interval(int letterSteps, int semitones) {
        this.letterSteps = letterSteps;
        this.semitones = semitones;
    }

    public int letterSteps() {
        return letterSteps;
    }

    public int semitones() {
        return semitones;
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
