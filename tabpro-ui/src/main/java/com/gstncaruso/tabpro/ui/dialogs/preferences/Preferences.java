package com.gstncaruso.tabpro.ui.dialogs.preferences;

import com.gstncaruso.tabpro.core.model.NoteValue;

/**
 * Las preferencias generales del editor. Esta ventana solo construye y
 * entrega el valor para que quien la abra decida donde guardarlo; los cinco
 * se guardan de verdad en com.gstncaruso.tabpro.ui.Preferences.
 */
public record Preferences(
        NoteValue defaultNoteValue,
        boolean autoScrollDuringPlayback,
        boolean showBassInChordName,
        boolean undoEnabled,
        int autosaveEvery) {

    public static Preferences defaults() {
        return new Preferences(NoteValue.QUARTER, true, true, true, 20);
    }

    public Preferences withDefaultNoteValue(NoteValue defaultNoteValue) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery);
    }

    public Preferences withAutoScrollDuringPlayback(boolean autoScrollDuringPlayback) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery);
    }

    public Preferences withUndoEnabled(boolean undoEnabled) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery);
    }

    public Preferences withAutosaveEvery(int autosaveEvery) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery);
    }
}
