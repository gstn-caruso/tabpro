package com.gstncaruso.tabpro.ui.instruments;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public record Scale(int rootPitchClass, Set<Integer> semitones) {

    private static final List<String> CHROMATIC_INTERVAL_LABELS =
            List.of("1", "b2", "2", "b3", "3", "4", "b5", "5", "b6", "6", "b7", "7");

    public Scale {
        if (rootPitchClass < 0 || rootPitchClass > 11) {
            throw new IllegalArgumentException("rootPitchClass must be between 0 and 11: " + rootPitchClass);
        }
        semitones = Set.copyOf(semitones);
    }

    public Scale(int rootPitchClass, ScaleType type) {
        this(rootPitchClass, type.semitones());
    }

    public static Scale of(int rootPitchClass, Collection<Integer> semitones) {
        return new Scale(rootPitchClass, Set.copyOf(semitones));
    }

    public static Scale cMajor() {
        return new Scale(0, ScaleType.MAJOR);
    }

    public boolean contains(int midiNumber) {
        return semitones.contains(semitoneFromRoot(midiNumber));
    }

    public String intervalLabelOf(int midiNumber) {
        return CHROMATIC_INTERVAL_LABELS.get(semitoneFromRoot(midiNumber));
    }

    public int degreeOf(int midiNumber) {
        List<Integer> ordenados = semitones.stream().sorted().toList();
        return ordenados.indexOf(semitoneFromRoot(midiNumber)) + 1;
    }

    private int semitoneFromRoot(int midiNumber) {
        return Math.floorMod(midiNumber - rootPitchClass, 12);
    }
}
