package com.gstncaruso.tabpro.format.tabledit;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.NoteEffects;

/** What a TablEdit note carries before knowing which string it falls on (the position supplies that). */
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
