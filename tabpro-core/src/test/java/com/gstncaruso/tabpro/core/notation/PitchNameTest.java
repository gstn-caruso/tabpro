package com.gstncaruso.tabpro.core.notation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import org.junit.jupiter.api.Test;

class PitchNameTest {

    @Test
    void namesTheNaturalNotes() {
        assertEquals("C", PitchName.of(new Pitch(60)).text());
        assertEquals("E", PitchName.of(new Pitch(64)).text());
        assertEquals("B", PitchName.of(new Pitch(59)).text());
    }

    @Test
    void namesTheAlteredNotesAsSharps() {
        assertEquals("C#", PitchName.of(new Pitch(61)).text());
        assertEquals("F#", PitchName.of(new Pitch(66)).text());
        assertTrue(PitchName.of(new Pitch(61)).sharp());
        assertFalse(PitchName.of(new Pitch(60)).sharp());
    }

    @Test
    void countsOctavesTheScientificWay() {
        assertEquals(4, PitchName.of(new Pitch(60)).octave());
        assertEquals(2, PitchName.of(new Pitch(40)).octave());
        assertEquals("C4", PitchName.of(new Pitch(60)).textWithOctave());
        assertEquals("E2", PitchName.of(new Pitch(40)).textWithOctave());
    }

    @Test
    void placesEachNoteOnItsDiatonicRung() {
        assertEquals(30, PitchName.of(new Pitch(64)).diatonicIndex());
        assertEquals(35, PitchName.of(new Pitch(72)).diatonicIndex());
        assertEquals(
                PitchName.of(new Pitch(61)).diatonicIndex(),
                PitchName.of(new Pitch(60)).diatonicIndex(),
                "un sostenido comparte el grado con su natural");
    }

    @Test
    void isTheSameNoteNameAnOctaveApart() {
        assertEquals(PitchName.of(new Pitch(60)).text(), PitchName.of(new Pitch(72)).text());
        assertEquals(7, PitchName.of(new Pitch(72)).diatonicIndex() - PitchName.of(new Pitch(60)).diatonicIndex());
    }
}
