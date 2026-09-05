package com.gstncaruso.tabpro.core.notation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClefTest {

    @Test
    void standardGuitarTuningUsesTrebleClef() {
        assertEquals(Clef.TREBLE, Clef.forTuning(Tuning.standard()));
    }

    @Test
    void aTuningWithAStringBelowLowGuitarEUsesBassClef() {
        Tuning bassTuning = new Tuning(List.of(
                new Pitch(43),
                new Pitch(38),
                new Pitch(33),
                new Pitch(28)));
        assertEquals(Clef.BASS, Clef.forTuning(bassTuning));
    }

    @Test
    void aLowestStringExactlyAtLowGuitarEUsesTrebleClef() {
        Tuning tuning = new Tuning(List.of(new Pitch(40)));
        assertEquals(Clef.TREBLE, Clef.forTuning(tuning));
    }

    @Test
    void stepOfDelegatesToStaffPosition() {
        // MI grave al aire de la guitarra (E2, MIDI 40) en clave de sol: step -7 (ver StaffPositionTest).
        assertEquals(-7, Clef.TREBLE.stepOf(new Pitch(40)));
    }
}
