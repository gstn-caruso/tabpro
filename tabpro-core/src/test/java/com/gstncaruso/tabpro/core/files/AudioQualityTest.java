package com.gstncaruso.tabpro.core.files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AudioQualityTest {

    @Test
    void standardIsCdQuality() {
        AudioQuality quality = AudioQuality.standard();

        assertEquals(44_100, quality.sampleRateHz());
        assertEquals(16, quality.bitDepth());
        assertEquals(2, quality.channels());
    }
}
