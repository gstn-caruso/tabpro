package com.gstncaruso.tabpro.ui.dialogs.preferences;

import com.gstncaruso.tabpro.core.model.NoteValue;

/**
 * Las preferencias generales del editor. Esta ventana solo construye y
 * entrega el valor para que quien la abra decida donde guardarlo; los seis
 * se guardan de verdad en com.gstncaruso.tabpro.ui.Preferences.
 */
public record Preferences(
        NoteValue defaultNoteValue,
        boolean autoScrollDuringPlayback,
        boolean showBassInChordName,
        boolean undoEnabled,
        int autosaveEvery,
        boolean forceMultitrackInHorizontalMode) {

    public static Preferences defaults() {
        return new Preferences(NoteValue.QUARTER, true, true, true, 20, false);
    }

    public Preferences withDefaultNoteValue(NoteValue defaultNoteValue) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode);
    }

    public Preferences withAutoScrollDuringPlayback(boolean autoScrollDuringPlayback) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode);
    }

    public Preferences withUndoEnabled(boolean undoEnabled) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode);
    }

    /** El manual: forzar la vista multipista al usar la pantalla horizontal. */
    public Preferences withForceMultitrackInHorizontalMode(boolean forceMultitrackInHorizontalMode) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode);
    }

    public Preferences withAutosaveEvery(int autosaveEvery) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode);
    }
}
