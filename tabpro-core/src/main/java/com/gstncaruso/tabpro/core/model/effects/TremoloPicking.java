package com.gstncaruso.tabpro.core.model.effects;

import com.gstncaruso.tabpro.core.model.NoteValue;

public record TremoloPicking(NoteValue speed) {

    public static TremoloPicking at(NoteValue speed) {
        return new TremoloPicking(speed);
    }
}
