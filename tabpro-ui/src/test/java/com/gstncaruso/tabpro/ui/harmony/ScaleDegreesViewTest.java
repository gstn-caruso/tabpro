package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.harmony.Interval;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.ScaleTone;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScaleDegreesViewTest {

    @Test
    void sinEscalaElegidaNoHayNingunGradoQuePintar() {
        ScaleDegreesView view = new ScaleDegreesView();

        assertEquals(0, view.degreeCount());
    }

    @Test
    void muestraElNombreDeNotaYElIntervaloDeCadaGrado() {
        ScaleDegreesView view = new ScaleDegreesView();

        view.show(List.of(
                new ScaleTone(PitchClass.of("C"), Interval.ROOT, 1),
                new ScaleTone(PitchClass.of("D"), Interval.MAJOR_SECOND, 2)));

        assertEquals(2, view.degreeCount());
        assertEquals("C", view.noteLabel(0));
        assertEquals("1", view.intervalLabel(0));
        assertEquals("D", view.noteLabel(1));
        assertEquals("2", view.intervalLabel(1));
    }
}
