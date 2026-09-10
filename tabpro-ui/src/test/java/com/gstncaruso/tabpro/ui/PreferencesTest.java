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
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock(RealPreferencesTests.SHARED_PREFERENCES_FILESYSTEM_NODE_LOCK)
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

    @Test
    void forcingMultitrackOnHorizontalScreenStartsOff() {
        assertFalse(preferences.forceMultitrackInHorizontalMode());
    }

    @Test
    void remembersWhetherToForceMultitrackOnHorizontalScreen() {
        preferences.setForceMultitrackInHorizontalMode(true);

        assertTrue(preferences.forceMultitrackInHorizontalMode());
    }

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

    @Test
    void remembersTheMetronomePreference() {
        preferences.setMetronomeEnabled(true);

        assertTrue(preferences.metronomeEnabled());
    }

    @Test
    void metronomeStartsOffByDefault() {
        assertFalse(preferences.metronomeEnabled());
    }

    @Test
    void interfaceFontSizeStartsAtTwelvePoints() {
        assertEquals(12, preferences.interfaceFontSize());
    }

    @Test
    void remembersTheInterfaceFontSize() {
        preferences.setInterfaceFontSize(16);

        assertEquals(16, preferences.interfaceFontSize());
    }

    @Test
    void highContrastStartsOff() {
        assertFalse(preferences.highContrastEnabled());
    }

    @Test
    void remembersThatHighContrastIsOn() {
        preferences.setHighContrastEnabled(true);

        assertTrue(preferences.highContrastEnabled());
    }

    @Test
    void animationsStartEnabled() {
        assertFalse(preferences.animationsDisabled());
    }

    @Test
    void remembersThatAnimationsAreDisabled() {
        preferences.setAnimationsDisabled(true);

        assertTrue(preferences.animationsDisabled());
    }

    @Test
    void effectsToolBarStartsVisible() {
        assertTrue(preferences.effectsToolBarVisible());
    }

    @Test
    void remembersThatTheEffectsToolBarIsHidden() {
        preferences.setEffectsToolBarVisible(false);

        assertFalse(preferences.effectsToolBarVisible());
    }

    @Test
    void fretboardStartsHidden() {
        assertFalse(preferences.fretboardVisible());
    }

    @Test
    void remembersThatTheFretboardIsVisible() {
        preferences.setFretboardVisible(true);

        assertTrue(preferences.fretboardVisible());
    }

    @Test
    void keyboardStartsHidden() {
        assertFalse(preferences.keyboardVisible());
    }

    @Test
    void remembersThatTheKeyboardIsVisible() {
        preferences.setKeyboardVisible(true);

        assertTrue(preferences.keyboardVisible());
    }
}
