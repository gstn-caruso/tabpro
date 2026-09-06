package com.gstncaruso.tabpro.ui.dialogs.metronome;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MetronomeSettingsTest {

    @Test
    void rejectsANegativeVolume() {
        assertThrows(IllegalArgumentException.class, () -> new MetronomeSettings(true, -1));
    }

    @Test
    void rejectsAVolumeAboveTheMidiRange() {
        assertThrows(IllegalArgumentException.class, () -> new MetronomeSettings(true, 128));
    }
}
