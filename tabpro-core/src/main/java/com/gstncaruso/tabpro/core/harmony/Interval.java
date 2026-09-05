package com.gstncaruso.tabpro.core.harmony;

import java.util.Arrays;
import java.util.Optional;

/**
 * Un intervalo con nombre: cuantas letras y cuantos semitonos hay desde la fundamental, y
 * como se escribe (b3, #5, 9...). Sirve tanto para deletrear las notas de un acorde como
 * para nombrar el grado de una escala.
 */
public enum Interval {
    ROOT(0, 0, "1"),
    MINOR_SECOND(1, 1, "b2"),
    MAJOR_SECOND(1, 2, "2"),
    MINOR_THIRD(2, 3, "b3"),
    MAJOR_THIRD(2, 4, "3"),
    PERFECT_FOURTH(3, 5, "4"),
    AUGMENTED_FOURTH(3, 6, "#4"),
    DIMINISHED_FIFTH(4, 6, "b5"),
    PERFECT_FIFTH(4, 7, "5"),
    AUGMENTED_FIFTH(4, 8, "#5"),
    MINOR_SIXTH(5, 8, "b6"),
    MAJOR_SIXTH(5, 9, "6"),
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

    /** Cuantas letras hay que subir desde la fundamental (0 a 6, sin contar octavas). */
    public int letterSteps() {
        return letterSteps;
    }

    /** Cuantos semitonos hay desde la fundamental. Puede pasar de 12 en las extensiones (9, 11, 13). */
    public int semitones() {
        return semitones;
    }

    public String label() {
        return label;
    }

    /** La nota que resulta de aplicar este intervalo a esa fundamental. */
    public PitchClass from(PitchClass root) {
        return root.steppedBy(letterSteps, semitones);
    }

    /** El intervalo simple (dentro de una octava) que corresponde a esos pasos y semitonos. */
    public static Optional<Interval> matching(int letterSteps, int semitones) {
        int steps = Math.floorMod(letterSteps, 7);
        int pitchClass = Math.floorMod(semitones, 12);
        return Arrays.stream(values())
                .filter(interval -> interval.letterSteps == steps && interval.semitones % 12 == pitchClass)
                .findFirst();
    }
}
