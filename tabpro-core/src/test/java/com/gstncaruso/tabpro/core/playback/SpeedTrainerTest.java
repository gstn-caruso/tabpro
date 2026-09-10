package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpeedTrainerTest {

    @Test
    void theFirstLapIsTheStartingTempo() {
        SpeedTrainer trainer = new SpeedTrainer(80, 140, 10);

        assertEquals(80, trainer.tempoForLap(0));
    }

    @Test
    void eachLapRaisesByTheIncrement() {
        SpeedTrainer trainer = new SpeedTrainer(80, 140, 10);

        assertEquals(90, trainer.tempoForLap(1));
        assertEquals(100, trainer.tempoForLap(2));
    }

    @Test
    void neverExceedsTheFinalTempo() {
        SpeedTrainer trainer = new SpeedTrainer(80, 100, 10);

        assertEquals(100, trainer.tempoForLap(5));
    }

    @Test
    void knowsWhenItReachedTheFinalTempo() {
        SpeedTrainer trainer = new SpeedTrainer(80, 100, 10);

        assertFalse(trainer.reachedFinalTempo(0));
        assertTrue(trainer.reachedFinalTempo(2));
    }

    @Test
    void rejectsAFinalTempoLowerThanTheStarting() {
        assertThrows(IllegalArgumentException.class, () -> new SpeedTrainer(140, 80, 10));
    }

    @Test
    void rejectsANonPositiveIncrement() {
        assertThrows(IllegalArgumentException.class, () -> new SpeedTrainer(80, 140, 0));
    }
}
