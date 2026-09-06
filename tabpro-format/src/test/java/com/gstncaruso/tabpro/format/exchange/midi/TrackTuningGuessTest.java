package com.gstncaruso.tabpro.format.exchange.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Tuning;
import org.junit.jupiter.api.Test;

class TrackTuningGuessTest {

    @Test
    void guessesBassFromTheGeneralMidiProgram() {
        assertEquals(Tuning.standardBass(), TrackTuningGuess.forQuickImport("Pista 2", 33));
    }

    @Test
    void guessesBassFromTheTrackName() {
        assertEquals(Tuning.standardBass(), TrackTuningGuess.forQuickImport("Bajo electrico", 0));
    }

    @Test
    void defaultsToStandardGuitarOtherwise() {
        assertEquals(Tuning.standard(), TrackTuningGuess.forQuickImport("Piano", 0));
    }
}
