package com.gstncaruso.tabpro.ui.sound;

import com.gstncaruso.tabpro.core.model.InstrumentPatch;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.Preferences;

/**
 * Lo que Options > MIDI Setup recuerda entre sesiones, guardado con
 * java.util.prefs igual que ChordLibrary: el dispositivo y el patch de
 * instrumentos de cada puerto, si limita la variacion de altura, la entrada
 * de captura, su sensibilidad y la asignacion de cuerdas.
 */
public final class MidiSetupPreferences {

    /** El mismo valor por defecto que MidiCapture.DEFAULT_SENSITIVITY_MILLIS -tabpro-ui no depende de tabpro-midi. */
    private static final int DEFAULT_SENSITIVITY_MILLIS = 60;

    private final Preferences store;

    public MidiSetupPreferences(Preferences store) {
        this.store = store;
    }

    public static MidiSetupPreferences userPreferences() {
        return new MidiSetupPreferences(Preferences.userNodeForPackage(MidiSetupPreferences.class).node("midiSetup"));
    }

    public String outputDevice(int port) {
        return store.get(key("output", port), "");
    }

    public void setOutputDevice(int port, String name) {
        store.put(key("output", port), name);
    }

    public String patchPath(int port) {
        return store.get(key("patch", port), "");
    }

    public void setPatchPath(int port, String path) {
        store.put(key("patch", port), path);
    }

    /** El patch cargado desde el archivo recordado, o General MIDI si no hay ninguno o no se puede leer. */
    public InstrumentPatch patch(int port) {
        String path = patchPath(port);
        if (path.isBlank()) {
            return InstrumentPatch.generalMidi();
        }
        try {
            return InstrumentPatch.parse(Files.readString(Path.of(path)));
        } catch (IOException e) {
            return InstrumentPatch.generalMidi();
        }
    }

    public boolean limitPitchVariation(int port) {
        return store.getBoolean(key("limit", port), false);
    }

    public void setLimitPitchVariation(int port, boolean limit) {
        store.putBoolean(key("limit", port), limit);
    }

    public String inputDevice() {
        return store.get("input", "");
    }

    public void setInputDevice(String name) {
        store.put("input", name);
    }

    public int sensitivityMillis() {
        return store.getInt("sensitivityMillis", DEFAULT_SENSITIVITY_MILLIS);
    }

    public void setSensitivityMillis(int millis) {
        store.putInt("sensitivityMillis", millis);
    }

    public StringAssignment stringAssignment() {
        String name = store.get("stringAssignment", StringAssignment.NO_CHANNEL_DETECTION.name());
        try {
            return StringAssignment.valueOf(name);
        } catch (IllegalArgumentException e) {
            return StringAssignment.NO_CHANNEL_DETECTION;
        }
    }

    public void setStringAssignment(StringAssignment assignment) {
        store.put("stringAssignment", assignment.name());
    }

    private static String key(String name, int port) {
        return name + port;
    }
}
