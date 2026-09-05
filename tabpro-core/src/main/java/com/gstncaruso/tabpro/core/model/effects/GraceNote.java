package com.gstncaruso.tabpro.core.model.effects;

import com.gstncaruso.tabpro.core.model.NoteValue;

/**
 * La nota muy corta que adorna a otra. No cuenta para la duracion del compas
 * ni para el dibujo del ritmo.
 */
public record GraceNote(
        int fret, NoteValue duration, Dynamic dynamic, GraceTransition transition, boolean onBeat, boolean dead) {

    public GraceNote {
        if (fret < 0) {
            throw new IllegalArgumentException("fret debe ser >= 0: " + fret);
        }
    }

    public static GraceNote before(int fret) {
        return new GraceNote(fret, NoteValue.THIRTY_SECOND, Dynamic.defaultDynamic(), GraceTransition.NONE, false, false);
    }
}
