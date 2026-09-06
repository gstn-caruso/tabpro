package com.gstncaruso.tabpro.core.model;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import com.gstncaruso.tabpro.core.model.effects.Velocity;

/**
 * Una nota de la tablatura: la cuerda que se pisa, el traste, y todo lo que se
 * le pide con los simbolos de la partitura.
 */
public record Note(int string, int fret, boolean tied, NoteEffects effects) {

    /** El digito mas grande que admite la tablatura; en percusion es un numero MIDI. */
    public static final int MAX_FRET = 99;

    /** Que fraccion de su figura suena una nota que se deja sonar. */
    public static final double LET_RING_LENGTH = 2.0;

    public Note {
        if (string < 1) {
            throw new IllegalArgumentException("string debe ser >= 1: " + string);
        }
        if (fret < 0 || fret > MAX_FRET) {
            throw new IllegalArgumentException("fret debe estar entre 0 y " + MAX_FRET + ": " + fret);
        }
    }

    public Note(int string, int fret) {
        this(string, fret, false, NoteEffects.none());
    }

    /** Una nota que no se ataca: suma su duracion a la anterior de la misma cuerda. */
    public static Note tiedOn(int string) {
        return new Note(string, 0, true, NoteEffects.none());
    }

    public boolean has(Ornament ornament) {
        return effects.has(ornament);
    }

    public Velocity velocity() {
        return effects.velocity();
    }

    public double soundLength() {
        return effects.soundLength();
    }

    public Note withFret(int fret) {
        return new Note(string, fret, tied, effects);
    }

    public Note onString(int string) {
        return new Note(string, fret, tied, effects);
    }

    public Note tied(boolean tied) {
        return new Note(string, fret, tied, effects);
    }

    public Note withEffects(NoteEffects effects) {
        return new Note(string, fret, tied, effects);
    }

    public Note toggling(Ornament ornament) {
        return withEffects(effects.toggling(ornament));
    }

    public Note withDynamic(Dynamic dynamic) {
        return withEffects(effects.withDynamic(dynamic));
    }

    public Note withBend(Bend bend) {
        return withEffects(effects.withBend(bend));
    }

    public Note withSlide(SlideType slide) {
        return withEffects(effects.withSlide(slide));
    }

    public Note withHarmonic(HarmonicType harmonic) {
        return withEffects(effects.withHarmonic(harmonic));
    }

    public Note transposed(int semitones) {
        return withFret(Math.clamp(fret + semitones, 0, MAX_FRET));
    }
}
