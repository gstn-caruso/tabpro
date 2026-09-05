package com.gstncaruso.tabpro.ui.dialogs.metronome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class MetronomePanelTest {

    @Test
    void startsWithTheScoresTempoAndTheGivenSettings() {
        MetronomePanel panel = new MetronomePanel(96, new MetronomeSettings(true, 80));

        assertEquals(96, panel.toTempo());
        assertEquals(new MetronomeSettings(true, 80), panel.toSettings());
    }

    @Test
    void reflectsWhateverIsLoadedAfterwards() {
        MetronomePanel panel = new MetronomePanel(120, MetronomeSettings.off());

        panel.apply(140, new MetronomeSettings(true, 60));

        assertEquals(140, panel.toTempo());
        assertEquals(new MetronomeSettings(true, 60), panel.toSettings());
    }

    @Test
    void defaultsToWhateverWasPassedIn() {
        MetronomePanel panel = new MetronomePanel(60, MetronomeSettings.off());

        assertFalse(panel.toSettings().active());
    }
}
