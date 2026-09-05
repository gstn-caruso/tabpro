package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import org.junit.jupiter.api.Test;

/** La cuenta regresiva: un compas vacio antes de empezar, si esta activada. */
class CountInTest {

    @Test
    void desactivadaNoAgregaTiempo() {
        assertEquals(0, CountIn.off().leadInTicks(TimeSignature.fourFour()));
    }

    @Test
    void activadaDuraUnCompasEntero() {
        assertEquals(TimeSignature.fourFour().ticksPerMeasure(), CountIn.on().leadInTicks(TimeSignature.fourFour()));
    }

    @Test
    void respetaLaMedidaDelCompas() {
        TimeSignature threeFour = new TimeSignature(3, 4);
        assertEquals(threeFour.ticksPerMeasure(), CountIn.on().leadInTicks(threeFour));
    }
}
