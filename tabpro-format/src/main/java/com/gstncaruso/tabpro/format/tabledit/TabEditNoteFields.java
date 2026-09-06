package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;

/**
 * Lo que trae una nota de TablEdit antes de saber en que cuerda cae (eso lo
 * pone la posicion). El tapping, el slap y el fade in son marcas del beat
 * entero en el modelo de tabpro, asi que viajan aparte de los efectos de nota.
 */
record TabEditNoteFields(
        int fret,
        boolean isGraceNote,
        int graceNoteFret,
        Duration duration,
        boolean tied,
        Dynamic dynamic,
        VoicePart voice,
        NoteEffects effects,
        boolean tapping,
        boolean slapping,
        boolean fadeIn) {
}
