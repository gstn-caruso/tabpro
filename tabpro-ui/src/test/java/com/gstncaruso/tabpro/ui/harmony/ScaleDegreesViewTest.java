package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ScaleDegreesViewTest {

    @Test
    void sinEscalaElegidaNoHayNingunGradoQuePintar() {
        ScaleDegreesView view = new ScaleDegreesView();

        assertEquals(0, view.degreeCount());
    }
}
