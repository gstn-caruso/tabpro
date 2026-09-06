package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** El tempo relativo: un coeficiente de x0.25 a x2 sobre el tempo del archivo. */
class RelativeTempoTest {

    @Test
    void elTempoNormalNoCambiaNada() {
        assertEquals(120, RelativeTempo.normal().apply(120));
    }

    @Test
    void loEscalaSegunElCoeficiente() {
        assertEquals(60, new RelativeTempo(0.5).apply(120));
        assertEquals(240, new RelativeTempo(2.0).apply(120));
    }

    @Test
    void rechazaCoeficientesFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> new RelativeTempo(0.1));
        assertThrows(IllegalArgumentException.class, () -> new RelativeTempo(2.1));
    }

    @Test
    void aplicadoATimelineCambiaElTempoYDejaElRestoIgual() {
        Timeline original = new Timeline(120, 960, java.util.List.of());

        Timeline scaled = new RelativeTempo(0.5).applyTo(original);

        assertEquals(60, scaled.tempoBpm());
        assertEquals(960, scaled.ticksPerQuarter());
    }

    @Test
    void nuncaBajaDeUnBpm() {
        assertEquals(1, new RelativeTempo(0.25).apply(1));
    }
}
