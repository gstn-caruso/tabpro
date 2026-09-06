package com.gstncaruso.tabpro.ui.instruments;

import java.util.Collection;
import java.util.Set;

/**
 * Una escala concreta para dibujar sobre el diapason o el teclado: su tonica
 * (clase de altura, 0 es Do) y los semitonos que la forman.
 */
public record Scale(int rootPitchClass, Set<Integer> semitones) {

    public Scale {
        if (rootPitchClass < 0 || rootPitchClass > 11) {
            throw new IllegalArgumentException("rootPitchClass debe estar entre 0 y 11: " + rootPitchClass);
        }
        semitones = Set.copyOf(semitones);
    }

    public Scale(int rootPitchClass, ScaleType type) {
        this(rootPitchClass, type.semitones());
    }

    /** La escala que eligio la ventana de escalas, con su biblioteca completa. */
    public static Scale of(int rootPitchClass, Collection<Integer> semitones) {
        return new Scale(rootPitchClass, Set.copyOf(semitones));
    }

    public static Scale cMajor() {
        return new Scale(0, ScaleType.MAJOR);
    }

    public boolean contains(int midiNumber) {
        return semitones.contains(Math.floorMod(midiNumber - rootPitchClass, 12));
    }
}
