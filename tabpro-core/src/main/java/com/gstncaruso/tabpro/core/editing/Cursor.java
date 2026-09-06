package com.gstncaruso.tabpro.core.editing;

import com.gstncaruso.tabpro.core.model.VoicePart;

/** Donde esta parada la edicion: pista, compas, voz, beat, cuerda y en que notacion se edita. */
public record Cursor(int track, int measure, VoicePart voice, int beat, int string, Notation notation) {

    public Cursor(int track, int measure, int beat, int string) {
        this(track, measure, VoicePart.LEAD, beat, string, Notation.TABLATURE);
    }

    public Cursor onTrack(int track) {
        return new Cursor(track, measure, voice, beat, string, notation);
    }

    public Cursor onMeasure(int measure) {
        return new Cursor(track, measure, voice, beat, string, notation);
    }

    public Cursor onVoice(VoicePart voice) {
        return new Cursor(track, measure, voice, beat, string, notation);
    }

    public Cursor onBeat(int beat) {
        return new Cursor(track, measure, voice, beat, string, notation);
    }

    public Cursor onString(int string) {
        return new Cursor(track, measure, voice, beat, string, notation);
    }

    public Cursor onNotation(Notation notation) {
        return new Cursor(track, measure, voice, beat, string, notation);
    }

    public Cursor at(int measure, int beat) {
        return new Cursor(track, measure, voice, beat, string, notation);
    }
}
