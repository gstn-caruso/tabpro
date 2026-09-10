package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class TrackSettingsTest {

    @Test
    void standardSettingsDoNotForceChannels11to16() {
        assertFalse(TrackSettings.standard(ScoreColor.rgb(0)).forceChannels11to16());
    }

    @Test
    void forceChannels11to16RoundTripsAndKeepsTheRest() {
        TrackSettings original = TrackSettings.standard(ScoreColor.rgb(0)).withCapo(3);

        TrackSettings updated = original.withForceChannels11to16(true);

        assertEquals(true, updated.forceChannels11to16());
        assertEquals(original.capo(), updated.capo());
        assertEquals(original.color(), updated.color());
        assertEquals(original.display(), updated.display());
    }
}
