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
        boolean forceMultitrackInHorizontalMode,
        int interfaceFontSize) {

    private static final int DEFAULT_INTERFACE_FONT_SIZE = 12;

    public static Preferences defaults() {
        return new Preferences(NoteValue.QUARTER, true, true, true, 20, false, DEFAULT_INTERFACE_FONT_SIZE);
    }

    /** Preexistente a Accesibilidad: la fuente de la interfaz queda en su tamano por defecto. */
    public Preferences(
            NoteValue defaultNoteValue,
            boolean autoScrollDuringPlayback,
            boolean showBassInChordName,
            boolean undoEnabled,
            int autosaveEvery,
            boolean forceMultitrackInHorizontalMode) {
        this(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName, undoEnabled, autosaveEvery,
                forceMultitrackInHorizontalMode, DEFAULT_INTERFACE_FONT_SIZE);
    }

    public Preferences withDefaultNoteValue(NoteValue defaultNoteValue) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize);
    }

    public Preferences withAutoScrollDuringPlayback(boolean autoScrollDuringPlayback) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize);
    }

    public Preferences withUndoEnabled(boolean undoEnabled) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize);
    }

    /** El manual: forzar la vista multipista al usar la pantalla horizontal. */
    public Preferences withForceMultitrackInHorizontalMode(boolean forceMultitrackInHorizontalMode) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize);
    }

    public Preferences withAutosaveEvery(int autosaveEvery) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize);
    }

    /** Preferencias [F12] > Accesibilidad: la fuente base de la interfaz, la aplica Theme. */
    public Preferences withInterfaceFontSize(int interfaceFontSize) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize);
    }
}
