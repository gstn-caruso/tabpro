package com.gstncaruso.tabpro.core.editing;

import com.gstncaruso.tabpro.core.model.VoicePart;

/** Donde esta parada la edicion: pista, compas, voz, beat y cuerda. */
public record Cursor(int track, int measure, VoicePart voice, int beat, int string) {

    public Cursor(int track, int measure, int beat, int string) {
        this(track, measure, VoicePart.LEAD, beat, string);
    }

    public Cursor onTrack(int track) {
        return new Cursor(track, measure, voice, beat, string);
    }

    public Cursor onMeasure(int measure) {
        return new Cursor(track, measure, voice, beat, string);
    }

    public Cursor onVoice(VoicePart voice) {
        return new Cursor(track, measure, voice, beat, string);
    }

    public Cursor onBeat(int beat) {
        return new Cursor(track, measure, voice, beat, string);
    }

    public Cursor onString(int string) {
        return new Cursor(track, measure, voice, beat, string);
    }

    public Cursor at(int measure, int beat) {
        return new Cursor(track, measure, voice, beat, string);
    }
}
