package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class InstrumentsTest {

    @Test
    void namesTheWholeGeneralMidiSet() {
        assertEquals(128, Instruments.names().size());
    }

    @Test
    void namesTheEndsOfTheSet() {
        assertEquals("Acoustic Grand Piano", Instruments.nameOf(0));
        assertEquals("Gunshot", Instruments.nameOf(127));
    }

    @Test
    void namesTheProgramsTheDefaultTracksUse() {
        assertEquals("Acoustic Guitar (steel)", Instruments.nameOf(Track.GUITAR_PROGRAM));
        assertEquals("Electric Bass (finger)", Instruments.nameOf(Track.BASS_PROGRAM));
    }

    @Test
    void rejectsAProgramOutsideTheSet() {
        assertThrows(IllegalArgumentException.class, () -> Instruments.nameOf(128));
        assertThrows(IllegalArgumentException.class, () -> Instruments.nameOf(-1));
    }
}
