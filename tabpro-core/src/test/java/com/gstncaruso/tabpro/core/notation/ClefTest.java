package com.gstncaruso.tabpro.core.notation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.List;
import java.util.Optional;
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
        assertEquals(-7, Clef.TREBLE.stepOf(new Pitch(40)));
    }

    @Test
    void pitchAtStepIsTheInverseOfStepOf() {
        assertEquals(Optional.of(new Pitch(40)), Clef.TREBLE.pitchAtStep(-7));
        assertEquals(Optional.of(new Pitch(41)), Clef.TREBLE.pitchAtStep(-6));
    }

    @Test
    void pitchAtStepIsEmptyWhenTheResultingMidiIsOutOfRange() {
        assertEquals(Optional.empty(), Clef.TREBLE.pitchAtStep(-1000));
    }
}
