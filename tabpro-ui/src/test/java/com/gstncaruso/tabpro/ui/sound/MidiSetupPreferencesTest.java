package com.gstncaruso.tabpro.ui.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Instruments;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MidiSetupPreferencesTest {

    private final Preferences scratch = Preferences.userRoot().node("tabpro-test/" + getClass().getSimpleName() + "/" + java.util.UUID.randomUUID());
    private final MidiSetupPreferences preferences = new MidiSetupPreferences(scratch);

    @AfterEach
    void clearsTheScratchNode() throws BackingStoreException {
        scratch.removeNode();
    }

    @Test
    void startsWithoutASoundFontFileChosen() {
        assertEquals("", preferences.soundFontFile());
    }

    @Test
    void remembersTheSoundFontFileChosen() {
        preferences.setSoundFontFile("/usr/share/sounds/sf2/FluidR3_GM.sf2");

        assertEquals("/usr/share/sounds/sf2/FluidR3_GM.sf2", preferences.soundFontFile());
    }

    @Test
    void theSoundFontStartsActive() {
        assertTrue(preferences.soundFontActive());
    }

    @Test
    void remembersThatTheSoundFontWasTurnedOff() {
        preferences.setSoundFontActive(false);

        assertFalse(preferences.soundFontActive());
    }

    @Test
    void everyPortStartsWithoutADeviceChosen() {
        for (int port = 1; port <= 4; port++) {
            assertEquals("", preferences.outputDevice(port));
        }
    }

    @Test
    void remembersTheDeviceOfEachPortIndependently() {
        preferences.setOutputDevice(1, "Interface A");
        preferences.setOutputDevice(4, "Interface B");

        assertEquals("Interface A", preferences.outputDevice(1));
        assertEquals("Interface B", preferences.outputDevice(4));
        assertEquals("", preferences.outputDevice(2));
        assertEquals("", preferences.outputDevice(3));
    }

    @Test
    void everyPortStartsWithoutLimitingPitchVariation() {
        assertFalse(preferences.limitPitchVariation(2));
    }

    @Test
    void remembersThatAPortLimitsPitchVariation() {
        preferences.setLimitPitchVariation(2, true);

        assertTrue(preferences.limitPitchVariation(2));
        assertFalse(preferences.limitPitchVariation(3));
    }

    @Test
    void withoutAPatchEveryPortShowsTheGeneralMidiNames() {
        assertEquals(Instruments.nameOf(0), preferences.patch(1).nameOf(0));
    }

    @Test
    void loadsThePatchFromTheRememberedFile(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        Path patchFile = tempDir.resolve("my-patch.txt");
        Files.writeString(patchFile, "Homemade requinto");
        preferences.setPatchPath(3, patchFile.toString());

        assertEquals("Homemade requinto", preferences.patch(3).nameOf(0));
        assertEquals(patchFile.toString(), preferences.patchPath(3));
    }

    @Test
    void aMissingPatchFileFallsBackToGeneralMidiInsteadOfFailing() {
        preferences.setPatchPath(1, "/does/not/exist/this/file.txt");

        assertEquals(Instruments.nameOf(5), preferences.patch(1).nameOf(5));
    }

    @Test
    void theDefaultSensitivityMatchesMidiCapturesDefault() {
        assertEquals(60, preferences.sensitivityMillis());
    }

    @Test
    void remembersAChosenSensitivity() {
        preferences.setSensitivityMillis(120);

        assertEquals(120, preferences.sensitivityMillis());
    }

    @Test
    void defaultsToNoChannelDetectionForTheStringAssignment() {
        assertEquals(StringAssignment.NO_CHANNEL_DETECTION, preferences.stringAssignment());
    }

    @Test
    void remembersTheChosenStringAssignment() {
        preferences.setStringAssignment(StringAssignment.FIRST_CHANNEL_IS_THE_HIGHEST_STRING);

        assertEquals(StringAssignment.FIRST_CHANNEL_IS_THE_HIGHEST_STRING, preferences.stringAssignment());
    }

    @Test
    void rememberTheInputDevice() {
        preferences.setInputDevice("MIDI keyboard");

        assertEquals("MIDI keyboard", preferences.inputDevice());
    }
}
