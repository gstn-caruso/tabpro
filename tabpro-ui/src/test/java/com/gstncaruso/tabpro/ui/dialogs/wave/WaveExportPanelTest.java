package com.gstncaruso.tabpro.ui.dialogs.wave;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.files.AudioQuality;
import org.junit.jupiter.api.Test;

class WaveExportPanelTest {

    @Test
    void startsWithTheGivenDefaults() {
        WaveExportPanel panel = new WaveExportPanel(new AudioQuality(48_000, 24, 1));

        assertEquals(new AudioQuality(48_000, 24, 1), panel.toAudioQuality());
    }

    @Test
    void reportsTheChosenSampleRate() {
        WaveExportPanel panel = new WaveExportPanel(AudioQuality.standard());

        panel.chooseSampleRate(48_000);

        assertEquals(48_000, panel.toAudioQuality().sampleRateHz());
    }

    @Test
    void reportsTheChosenBitDepth() {
        WaveExportPanel panel = new WaveExportPanel(AudioQuality.standard());

        panel.chooseBitDepth(24);

        assertEquals(24, panel.toAudioQuality().bitDepth());
    }

    @Test
    void reportsMonoWhenChosen() {
        WaveExportPanel panel = new WaveExportPanel(AudioQuality.standard());

        panel.chooseMono();

        assertEquals(1, panel.toAudioQuality().channels());
    }

    @Test
    void reportsStereoWhenChosen() {
        WaveExportPanel panel = new WaveExportPanel(new AudioQuality(44_100, 16, 1));

        panel.chooseStereo();

        assertEquals(2, panel.toAudioQuality().channels());
    }
}
