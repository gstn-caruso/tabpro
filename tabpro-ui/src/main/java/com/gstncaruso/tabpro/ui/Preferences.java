package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.model.NoteValue;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lo que la ventana recuerda entre una sesion y la siguiente: la lista de
 * archivos recientes, el guardado automatico y las opciones de la ventana de
 * preferencias que describe el manual.
 */
public final class Preferences {

    public static final int MAX_RECENT_FILES = 8;

    private static final String RECENT_FILES = "recentFiles";
    private static final String AUTOSAVE_EVERY = "autosaveEvery";
    private static final String UNDO_ENABLED = "undoEnabled";
    private static final String METRONOME_ENABLED = "metronomeEnabled";
    private static final String DEFAULT_NOTE_VALUE = "defaultNoteValue";
    private static final String AUTO_SCROLL_DURING_PLAYBACK = "autoScrollDuringPlayback";
    private static final String VIEW_MODE = "viewMode";
    private static final String ZOOM = "zoom";
    private static final String FORCE_MULTITRACK_IN_HORIZONTAL_MODE = "forceMultitrackInHorizontalMode";
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

    public void forgetRecentFiles() {
        stored.remove(RECENT_FILES);
    }

    /** Cada cuantas acciones se guarda solo; cero significa que no se guarda. */
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

    /** Preferencias [F12], "Figura por defecto al insertar": la usa {@code Editor.insertBeat}. */
    public NoteValue defaultNoteValue() {
        return NoteValue.valueOf(stored.get(DEFAULT_NOTE_VALUE, NoteValue.QUARTER.name()));
    }

    public void setDefaultNoteValue(NoteValue defaultNoteValue) {
        stored.put(DEFAULT_NOTE_VALUE, defaultNoteValue.name());
    }

    /** Preferencias [F12], "Desplazar la pantalla durante la reproduccion": la usa ScoreCanvas. */
    public boolean autoScrollDuringPlayback() {
        return stored.getBoolean(AUTO_SCROLL_DURING_PLAYBACK, true);
    }

    public void setAutoScrollDuringPlayback(boolean autoScrollDuringPlayback) {
        stored.putBoolean(AUTO_SCROLL_DURING_PLAYBACK, autoScrollDuringPlayback);
    }

    public String viewMode() {
        return stored.get(VIEW_MODE, "PAGE");
    }

    public void setViewMode(String mode) {
        stored.put(VIEW_MODE, mode);
    }

    public int zoomPercent() {
        return stored.getInt(ZOOM, 100);
    }

    public void setZoomPercent(int percent) {
        stored.putInt(ZOOM, Math.clamp(percent, 30, 200));
    }

    /** El manual: forzar la vista multipista al usar la pantalla horizontal. Apagado por defecto. */
    public boolean forceMultitrackInHorizontalMode() {
        return stored.getBoolean(FORCE_MULTITRACK_IN_HORIZONTAL_MODE, false);
    }

    public void setForceMultitrackInHorizontalMode(boolean forced) {
        stored.putBoolean(FORCE_MULTITRACK_IN_HORIZONTAL_MODE, forced);
    }
}
