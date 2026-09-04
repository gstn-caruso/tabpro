package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class TuningTest {

    @Test
    void standardTuningHasSixStrings() {
        assertEquals(6, Tuning.standard().stringCount());
    }

    @Test
    void standardTuningStringOneIsHighE() {
        assertEquals(new Pitch(64), Tuning.standard().pitchOfString(1));
    }

    @Test
    void standardTuningStringSixIsLowE() {
        assertEquals(new Pitch(40), Tuning.standard().pitchOfString(6));
    }

    @Test
    void pitchOfAnOpenStringIsTheStringPitch() {
        assertEquals(new Pitch(50), Tuning.standard().pitchOf(new Note(4, 0)));
    }

    @Test
    void pitchOfAFrettedNoteAddsTheFretToTheStringPitch() {
        assertEquals(new Pitch(52), Tuning.standard().pitchOf(new Note(4, 2)));
    }

    @Test
    void rejectsStringZero() {
        assertThrows(IllegalArgumentException.class, () -> Tuning.standard().pitchOfString(0));
    }

    @Test
    void rejectsAStringBeyondTheLast() {
        assertThrows(IllegalArgumentException.class, () -> Tuning.standard().pitchOfString(7));
    }

    @Test
    void rejectsATuningWithoutStrings() {
        assertThrows(IllegalArgumentException.class, () -> new Tuning(List.of()));
    }
}
