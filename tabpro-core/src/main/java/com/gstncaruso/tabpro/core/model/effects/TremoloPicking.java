package com.gstncaruso.tabpro.core.model.effects;

import com.gstncaruso.tabpro.core.model.NoteValue;

/** Repetir la misma nota lo mas rapido que se pueda, anotada con una sola cabeza. */
public record TremoloPicking(NoteValue speed) {

    public static TremoloPicking at(NoteValue speed) {
        return new TremoloPicking(speed);
    }
}
