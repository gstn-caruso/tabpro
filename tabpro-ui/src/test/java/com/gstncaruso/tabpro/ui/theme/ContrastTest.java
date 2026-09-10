package com.gstncaruso.tabpro.ui.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import org.junit.jupiter.api.Test;

class ContrastTest {

    @Test
    void blackAgainstWhiteIsTheWidestRatioPossible() {
        assertEquals(21.0, Contrast.ratio(Color.WHITE, Color.BLACK), 0.001);
    }
}
