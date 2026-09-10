package com.gstncaruso.tabpro.ui.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import org.junit.jupiter.api.Test;

class ContrastTest {

    @Test
    void blackAgainstWhiteIsTheWidestRatioPossible() {
        assertEquals(21.0, Contrast.ratio(Color.WHITE, Color.BLACK), 0.001);
    }

    @Test
    void aColourAgainstItselfNeverContrasts() {
        Color orange = new Color(0xE8A33D);
        assertEquals(1.0, Contrast.ratio(orange, orange), 0.001);
    }
}
