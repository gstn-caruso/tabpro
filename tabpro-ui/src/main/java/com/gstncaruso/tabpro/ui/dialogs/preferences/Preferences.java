package com.gstncaruso.tabpro.ui.dialogs.preferences;

import com.gstncaruso.tabpro.core.model.NoteValue;

public record Preferences(
        NoteValue defaultNoteValue,
        boolean autoScrollDuringPlayback,
        boolean showBassInChordName,
        boolean undoEnabled,
        int autosaveEvery,
        boolean forceMultitrackInHorizontalMode,
        int interfaceFontSize,
        boolean highContrastEnabled,
        boolean animationsDisabled) {

    private static final int DEFAULT_INTERFACE_FONT_SIZE = 12;

    public static Preferences defaults() {
        return new Preferences(
                NoteValue.QUARTER, true, true, true, 20, false, DEFAULT_INTERFACE_FONT_SIZE, false, false);
    }

    public Preferences(
            NoteValue defaultNoteValue,
            boolean autoScrollDuringPlayback,
            boolean showBassInChordName,
            boolean undoEnabled,
            int autosaveEvery,
            boolean forceMultitrackInHorizontalMode) {
        this(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName, undoEnabled, autosaveEvery,
                forceMultitrackInHorizontalMode, DEFAULT_INTERFACE_FONT_SIZE, false, false);
    }

    public Preferences withDefaultNoteValue(NoteValue defaultNoteValue) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize,
                highContrastEnabled, animationsDisabled);
    }

    public Preferences withAutoScrollDuringPlayback(boolean autoScrollDuringPlayback) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize,
                highContrastEnabled, animationsDisabled);
    }

    public Preferences withUndoEnabled(boolean undoEnabled) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize,
                highContrastEnabled, animationsDisabled);
    }

    public Preferences withForceMultitrackInHorizontalMode(boolean forceMultitrackInHorizontalMode) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize,
                highContrastEnabled, animationsDisabled);
    }

    public Preferences withAutosaveEvery(int autosaveEvery) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize,
                highContrastEnabled, animationsDisabled);
    }

    public Preferences withInterfaceFontSize(int interfaceFontSize) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize,
                highContrastEnabled, animationsDisabled);
    }

    public Preferences withHighContrastEnabled(boolean highContrastEnabled) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize,
                highContrastEnabled, animationsDisabled);
    }

    public Preferences withAnimationsDisabled(boolean animationsDisabled) {
        return new Preferences(defaultNoteValue, autoScrollDuringPlayback, showBassInChordName,
                undoEnabled, autosaveEvery, forceMultitrackInHorizontalMode, interfaceFontSize,
                highContrastEnabled, animationsDisabled);
    }
}
