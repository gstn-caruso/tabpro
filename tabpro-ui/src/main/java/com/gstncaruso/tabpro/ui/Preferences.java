package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.ui.i18n.Language;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Preferences {

    public static final int MAX_RECENT_FILES = 8;

    private static final String RECENT_FILES = "recentFiles";
    private static final String AUTOSAVE_EVERY = "autosaveEvery";
    private static final String UNDO_ENABLED = "undoEnabled";
    private static final String METRONOME_ENABLED = "metronomeEnabled";
    private static final String INTERFACE_FONT_SIZE = "interfaceFontSize";
    private static final int DEFAULT_INTERFACE_FONT_SIZE = 12;
    private static final String HIGH_CONTRAST_ENABLED = "highContrastEnabled";
    private static final String ANIMATIONS_DISABLED = "animationsDisabled";
    private static final String DEFAULT_NOTE_VALUE = "defaultNoteValue";
    private static final String AUTO_SCROLL_DURING_PLAYBACK = "autoScrollDuringPlayback";
    private static final String FORCE_MULTITRACK_IN_HORIZONTAL_MODE = "forceMultitrackInHorizontalMode";
    private static final String EFFECTS_TOOL_BAR_VISIBLE = "effectsToolBarVisible";
    private static final String FRETBOARD_VISIBLE = "fretboardVisible";
    private static final String KEYBOARD_VISIBLE = "keyboardVisible";
    private static final String SEPARATOR = "\n";

    private final java.util.prefs.Preferences stored;

    public Preferences() {
        this(java.util.prefs.Preferences.userRoot().node("com/gstncaruso/tabpro"));
    }

    public Preferences(java.util.prefs.Preferences stored) {
        this.stored = stored;
    }

    public List<Path> recentFiles() {
        String saved = stored.get(RECENT_FILES, "");
        if (saved.isBlank()) {
            return List.of();
        }
        return Arrays.stream(saved.split(SEPARATOR)).filter(line -> !line.isBlank()).map(Path::of).toList();
    }

    public void remember(Path path) {
        List<Path> recent = new ArrayList<>(recentFiles());
        recent.remove(path);
        recent.addFirst(path);
        while (recent.size() > MAX_RECENT_FILES) {
            recent.removeLast();
        }
        stored.put(RECENT_FILES, String.join(SEPARATOR, recent.stream().map(Path::toString).toList()));
    }

    public int autosaveEvery() {
        return stored.getInt(AUTOSAVE_EVERY, 20);
    }

    public void setAutosaveEvery(int actions) {
        stored.putInt(AUTOSAVE_EVERY, Math.max(0, actions));
    }

    public boolean undoEnabled() {
        return stored.getBoolean(UNDO_ENABLED, true);
    }

    public void setUndoEnabled(boolean enabled) {
        stored.putBoolean(UNDO_ENABLED, enabled);
    }

    public boolean metronomeEnabled() {
        return stored.getBoolean(METRONOME_ENABLED, false);
    }

    public void setMetronomeEnabled(boolean enabled) {
        stored.putBoolean(METRONOME_ENABLED, enabled);
    }

    public int interfaceFontSize() {
        return stored.getInt(INTERFACE_FONT_SIZE, DEFAULT_INTERFACE_FONT_SIZE);
    }

    public void setInterfaceFontSize(int points) {
        stored.putInt(INTERFACE_FONT_SIZE, points);
    }

    public boolean highContrastEnabled() {
        return stored.getBoolean(HIGH_CONTRAST_ENABLED, false);
    }

    public void setHighContrastEnabled(boolean enabled) {
        stored.putBoolean(HIGH_CONTRAST_ENABLED, enabled);
    }

    public boolean animationsDisabled() {
        return stored.getBoolean(ANIMATIONS_DISABLED, false);
    }

    public void setAnimationsDisabled(boolean disabled) {
        stored.putBoolean(ANIMATIONS_DISABLED, disabled);
    }

    public NoteValue defaultNoteValue() {
        return NoteValue.valueOf(stored.get(DEFAULT_NOTE_VALUE, NoteValue.QUARTER.name()));
    }

    public void setDefaultNoteValue(NoteValue defaultNoteValue) {
        stored.put(DEFAULT_NOTE_VALUE, defaultNoteValue.name());
    }

    public boolean autoScrollDuringPlayback() {
        return stored.getBoolean(AUTO_SCROLL_DURING_PLAYBACK, true);
    }

    public void setAutoScrollDuringPlayback(boolean autoScrollDuringPlayback) {
        stored.putBoolean(AUTO_SCROLL_DURING_PLAYBACK, autoScrollDuringPlayback);
    }

    public boolean forceMultitrackInHorizontalMode() {
        return stored.getBoolean(FORCE_MULTITRACK_IN_HORIZONTAL_MODE, false);
    }

    public void setForceMultitrackInHorizontalMode(boolean forced) {
        stored.putBoolean(FORCE_MULTITRACK_IN_HORIZONTAL_MODE, forced);
    }

    public boolean effectsToolBarVisible() {
        return stored.getBoolean(EFFECTS_TOOL_BAR_VISIBLE, true);
    }

    public void setEffectsToolBarVisible(boolean visible) {
        stored.putBoolean(EFFECTS_TOOL_BAR_VISIBLE, visible);
    }

    public boolean fretboardVisible() {
        return stored.getBoolean(FRETBOARD_VISIBLE, false);
    }

    public void setFretboardVisible(boolean visible) {
        stored.putBoolean(FRETBOARD_VISIBLE, visible);
    }

    public boolean keyboardVisible() {
        return stored.getBoolean(KEYBOARD_VISIBLE, false);
    }

    public void setKeyboardVisible(boolean visible) {
        stored.putBoolean(KEYBOARD_VISIBLE, visible);
    }

    public Language interfaceLanguage() {
        return Language.AUTOMATIC;
    }
}
