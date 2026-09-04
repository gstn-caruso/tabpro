package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TimeSignatureTest {

    @Test
    void fourFourLastsFourQuarters() {
        assertEquals(Duration.TICKS_PER_QUARTER * 4L, TimeSignature.fourFour().ticksPerMeasure());
    }

    @Test
    void threeFourLastsThreeQuarters() {
        assertEquals(Duration.TICKS_PER_QUARTER * 3L, new TimeSignature(3, 4).ticksPerMeasure());
    }

    @Test
    void sixEightLastsThreeQuarters() {
        assertEquals(Duration.TICKS_PER_QUARTER * 3L, new TimeSignature(6, 8).ticksPerMeasure());
    }

    @Test
    void rejectsZeroBeats() {
        assertThrows(IllegalArgumentException.class, () -> new TimeSignature(0, 4));
    }

    @Test
    void rejectsANonPowerOfTwoUnit() {
        assertThrows(IllegalArgumentException.class, () -> new TimeSignature(4, 3));
    }
}
