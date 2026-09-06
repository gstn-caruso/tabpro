package com.gstncaruso.tabpro.ui.dialogs.preferences;

import com.gstncaruso.tabpro.core.model.NoteValue;

/**
 * Las preferencias generales del editor. Todavia no hay un lugar en tabpro-core
 * donde vivan de forma permanente: esta ventana solo construye y entrega el
 * valor para que quien la abra decida donde guardarlo.
 */
public record Preferences(
        NoteValue defaultNoteValue, boolean countIn, boolean autoScrollDuringPlayback, boolean showBassInChordName) {

    public static Preferences defaults() {
        return new Preferences(NoteValue.QUARTER, false, true, true);
    }
}
