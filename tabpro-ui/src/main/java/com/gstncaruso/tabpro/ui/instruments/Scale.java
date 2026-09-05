package com.gstncaruso.tabpro.ui.instruments;

/** Una escala concreta: su tonica (clase de altura, 0 es Do) y su patron de intervalos. */
public record Scale(int rootPitchClass, ScaleType type) {

    public Scale {
        if (rootPitchClass < 0 || rootPitchClass > 11) {
            throw new IllegalArgumentException("rootPitchClass debe estar entre 0 y 11: " + rootPitchClass);
        }
    }

    public static Scale cMajor() {
        return new Scale(0, ScaleType.MAJOR);
    }

    public boolean contains(int midiNumber) {
        return type.has(midiNumber - rootPitchClass);
    }
}
