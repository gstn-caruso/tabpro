package com.gstncaruso.tabpro.ui.dialogs.preferences;

import com.gstncaruso.tabpro.core.model.NoteValue;

/**
 * Las preferencias generales del editor. Esta ventana solo construye y
 * entrega el valor para que quien la abra decida donde guardarlo; undoEnabled
 * y autosaveEvery se guardan de verdad en com.gstncaruso.tabpro.ui.Preferences.
 */
public record Preferences(
        NoteValue defaultNoteValue,
        boolean countIn,
        boolean autoScrollDuringPlayback,
        boolean showBassInChordName,
        boolean undoEnabled,
        int autosaveEvery) {

    /** Compatibilidad con quien todavia no conoce undoEnabled ni autosaveEvery. */
    public Preferences(
            NoteValue defaultNoteValue, boolean countIn, boolean autoScrollDuringPlayback, boolean showBassInChordName) {
        this(defaultNoteValue, countIn, autoScrollDuringPlayback, showBassInChordName, true, 20);
    }

    public static Preferences defaults() {
        return new Preferences(NoteValue.QUARTER, false, true, true, true, 20);
    }

    public Preferences withUndoEnabled(boolean undoEnabled) {
        return new Preferences(defaultNoteValue, countIn, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery);
    }

    public Preferences withAutosaveEvery(int autosaveEvery) {
        return new Preferences(defaultNoteValue, countIn, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery);
    }
}
