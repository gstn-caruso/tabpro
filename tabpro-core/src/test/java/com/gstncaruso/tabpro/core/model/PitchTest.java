package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PitchTest {

    @Test
    void transposesBySemitones() {
        assertEquals(new Pitch(64), new Pitch(60).transposed(4));
    }

    @Test
    void rejectsMidiNumbersBelowZero() {
        assertThrows(IllegalArgumentException.class, () -> new Pitch(-1));
    }

    @Test
    void rejectsMidiNumbersAbove127() {
        assertThrows(IllegalArgumentException.class, () -> new Pitch(128));
    }
}
