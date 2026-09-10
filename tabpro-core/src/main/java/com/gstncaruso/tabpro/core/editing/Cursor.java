package com.gstncaruso.tabpro.core.editing;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.VoicePart;
import java.util.Optional;

public record Cursor(int track, int measure, VoicePart voice, int beat, int string, Notation notation,
        Optional<Pitch> pointer) {

    public Cursor(int track, int measure, int beat, int string) {
        this(track, measure, VoicePart.LEAD, beat, string, Notation.TABLATURE, Optional.empty());
    }

    public Cursor onTrack(int track) {
        return new Cursor(track, measure, voice, beat, string, notation, Optional.empty());
    }

    public Cursor onMeasure(int measure) {
        return new Cursor(track, measure, voice, beat, string, notation, Optional.empty());
    }

    public Cursor onVoice(VoicePart voice) {
        return new Cursor(track, measure, voice, beat, string, notation, pointer);
    }

    public Cursor onBeat(int beat) {
        return new Cursor(track, measure, voice, beat, string, notation, Optional.empty());
    }

    public Cursor onString(int string) {
        return new Cursor(track, measure, voice, beat, string, notation, pointer);
    }

    public Cursor onNotation(Notation notation) {
        return new Cursor(track, measure, voice, beat, string, notation, Optional.empty());
    }

    public Cursor withPointer(Pitch pointer) {
        return new Cursor(track, measure, voice, beat, string, notation, Optional.of(pointer));
    }

    public Cursor at(int measure, int beat) {
        return new Cursor(track, measure, voice, beat, string, notation, Optional.empty());
    }
}
