package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * El speed trainer: cada vuelta del loop sube el tempo un paso, sin pasarse
 * del tempo final.
 */
class SpeedTrainerTest {

    @Test
    void laPrimeraVueltaEsElTempoInicial() {
        SpeedTrainer trainer = new SpeedTrainer(80, 140, 10);

        assertEquals(80, trainer.tempoForLap(0));
    }

    @Test
    void cadaVueltaSubeElIncremento() {
        SpeedTrainer trainer = new SpeedTrainer(80, 140, 10);

        assertEquals(90, trainer.tempoForLap(1));
        assertEquals(100, trainer.tempoForLap(2));
    }

    @Test
    void nuncaSuperaElTempoFinal() {
        SpeedTrainer trainer = new SpeedTrainer(80, 100, 10);

        assertEquals(100, trainer.tempoForLap(5));
    }

    @Test
    void sabeCuandoLlegoAlTempoFinal() {
        SpeedTrainer trainer = new SpeedTrainer(80, 100, 10);

        assertFalse(trainer.reachedFinalTempo(0));
        assertTrue(trainer.reachedFinalTempo(2));
    }

    @Test
    void rechazaUnTempoFinalMenorAlInicial() {
        assertThrows(IllegalArgumentException.class, () -> new SpeedTrainer(140, 80, 10));
    }

    @Test
    void rechazaUnIncrementoNoPositivo() {
        assertThrows(IllegalArgumentException.class, () -> new SpeedTrainer(80, 140, 0));
    }
}
