package com.gstncaruso.tabpro.ui.instruments;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Una escala concreta para dibujar sobre el diapason o el teclado: su tonica
 * (clase de altura, 0 es Do) y los semitonos que la forman.
 */
public record Scale(int rootPitchClass, Set<Integer> semitones) {

    /**
     * Como se nombra cada semitono cromatico desde la tonica (1, b2, 2, b3, 3, 4, b5, 5, b6, 6,
     * b7, 7): el nombre generico de intervalo, independiente de si esa nota es de la escala.
     */
    private static final List<String> CHROMATIC_INTERVAL_LABELS =
            List.of("1", "b2", "2", "b3", "3", "4", "b5", "5", "b6", "6", "b7", "7");

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
        return semitones.contains(semitoneFromRoot(midiNumber));
    }

    /** El intervalo de esa nota respecto de la tonica, se toque o no en esta escala. */
    public String intervalLabelOf(int midiNumber) {
        return CHROMATIC_INTERVAL_LABELS.get(semitoneFromRoot(midiNumber));
    }

    /** Que lugar ocupa esa nota dentro de la escala: 1 es la tonica, 2 la siguiente... */
    public int degreeOf(int midiNumber) {
        List<Integer> ordenados = semitones.stream().sorted().toList();
        return ordenados.indexOf(semitoneFromRoot(midiNumber)) + 1;
    }

    private int semitoneFromRoot(int midiNumber) {
        return Math.floorMod(midiNumber - rootPitchClass, 12);
    }
}
