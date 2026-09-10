package com.gstncaruso.tabpro.core.model.effects;

import com.gstncaruso.tabpro.core.model.NoteValue;

public record Trill(int fret, NoteValue speed) {

    public Trill {
        if (fret < 0) {
            throw new IllegalArgumentException("fret debe ser >= 0: " + fret);
        }
    }

    public static Trill to(int fret) {
        return new Trill(fret, NoteValue.THIRTY_SECOND);
    }
}
