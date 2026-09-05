package com.gstncaruso.tabpro.core.notation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.VoicePart;
import org.junit.jupiter.api.Test;

class StemDirectionTest {

    private static final double MIDDLE = 4.0;

    @Test
    void withOneVoiceALowBeatPointsUp() {
        assertTrue(StemDirection.pointsUp(VoicePart.LEAD, false, 1.0, MIDDLE));
    }

    @Test
    void withOneVoiceAHighBeatPointsDown() {
        assertFalse(StemDirection.pointsUp(VoicePart.LEAD, false, 7.0, MIDDLE));
    }

    @Test
    void withTwoVoicesTheLeadAlwaysPointsUpRegardlessOfRegister() {
        assertTrue(StemDirection.pointsUp(VoicePart.LEAD, true, 20.0, MIDDLE));
    }

    @Test
    void withTwoVoicesTheBassAlwaysPointsDownRegardlessOfRegister() {
        assertFalse(StemDirection.pointsUp(VoicePart.BASS, true, -20.0, MIDDLE));
    }
}
