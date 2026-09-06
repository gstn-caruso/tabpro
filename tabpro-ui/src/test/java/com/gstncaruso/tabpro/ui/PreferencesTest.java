package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.NoteValue;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PreferencesTest {

    private final java.util.prefs.Preferences node =
            java.util.prefs.Preferences.userRoot().node("com/gstncaruso/tabpro/test/" + UUID.randomUUID());
    private final Preferences preferences = new Preferences(node);

    @AfterEach
    void cleanUp() throws Exception {
        node.removeNode();
    }

    @Test
    void startsWithoutRecentFiles() {
        assertEquals(List.of(), preferences.recentFiles());
    }

    @Test
    void theLastFileOpenedComesFirst() {
        preferences.remember(Path.of("/tmp/una.tabpro"));
        preferences.remember(Path.of("/tmp/otra.tabpro"));

        assertEquals(List.of(Path.of("/tmp/otra.tabpro"), Path.of("/tmp/una.tabpro")), preferences.recentFiles());
    }

    @Test
    void openingTheSameFileTwiceDoesNotListItTwice() {
        preferences.remember(Path.of("/tmp/una.tabpro"));
        preferences.remember(Path.of("/tmp/otra.tabpro"));
        preferences.remember(Path.of("/tmp/una.tabpro"));

        assertEquals(List.of(Path.of("/tmp/una.tabpro"), Path.of("/tmp/otra.tabpro")), preferences.recentFiles());
    }

    @Test
    void theListDoesNotGrowPastItsLimit() {
        for (int index = 0; index < Preferences.MAX_RECENT_FILES + 5; index++) {
            preferences.remember(Path.of("/tmp/score" + index + ".tabpro"));
        }

        assertEquals(Preferences.MAX_RECENT_FILES, preferences.recentFiles().size());
    }

    /**
     * El manual: "You can force the multitrack view when using the Horizontal Screen Mode".
     * Apagada por defecto, para no cambiar lo que ya se ve hoy.
     */
    @Test
    void forcingMultitrackOnHorizontalScreenStartsOff() {
        assertFalse(preferences.forceMultitrackInHorizontalMode());
    }

    @Test
    void remembersWhetherToForceMultitrackOnHorizontalScreen() {
        preferences.setForceMultitrackInHorizontalMode(true);

        assertTrue(preferences.forceMultitrackInHorizontalMode());
    }

    /**
     * Preferencias [F12]: "Figura por defecto al insertar" y "Desplazar la pantalla durante la
     * reproduccion" se quedaban solo en memoria -{@code editingPreferences} en MainFrame- y se
     * olvidaban al cerrar el programa. Tienen que persistir aca, igual que undoEnabled y
     * autosaveEvery.
     */
    @Test
    void remembersTheDefaultNoteValueAndTheAutoScrollPreference() {
        preferences.setDefaultNoteValue(NoteValue.EIGHTH);
        preferences.setAutoScrollDuringPlayback(false);

        assertEquals(NoteValue.EIGHTH, preferences.defaultNoteValue());
        assertFalse(preferences.autoScrollDuringPlayback());
    }

    @Test
    void defaultNoteValueAndAutoScrollStartAtAQuarterAndOn() {
        assertEquals(NoteValue.QUARTER, preferences.defaultNoteValue());
        assertTrue(preferences.autoScrollDuringPlayback());
    }

    /**
     * Manual, linea 2151: la pestaña General de Preferencias [F12] configura el metronomo.
     * MainFrame siembra {@link Transport} con este valor al arrancar.
     */
    @Test
    void remembersTheMetronomePreference() {
        preferences.setMetronomeEnabled(true);

        assertTrue(preferences.metronomeEnabled());
    }

    @Test
    void metronomeStartsOffByDefault() {
        assertFalse(preferences.metronomeEnabled());
    }
}
