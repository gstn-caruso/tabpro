package com.gstncaruso.tabpro.core.model;

public record Pitch(int midiNumber) {

    public Pitch {
        if (midiNumber < 0 || midiNumber > 127) {
            throw new IllegalArgumentException("midiNumber debe estar entre 0 y 127: " + midiNumber);
        }
    }

    public Pitch transposed(int semitones) {
        return new Pitch(midiNumber + semitones);
    }
}
