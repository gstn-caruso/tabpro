package com.gstncaruso.tabpro.app;

import org.junit.jupiter.api.Test;

/**
 * Preferencias [F12] > Accesibilidad > Alto contraste: 7:1 para texto (WCAG AAA) y 4.5:1 para
 * graficos, mas estricto que los dos temas normales.
 */
class HighContrastPaletteContrastTest {

    private static final double TEXT_MINIMUM_RATIO = 7.0;
    private static final double GRAPHICAL_MINIMUM_RATIO = 4.5;

    @Test
    void theHighContrastPaletteReadsOverItself() {
        PaletteContrastAssertions.assertReads(Theme.highContrastPalette(), TEXT_MINIMUM_RATIO, GRAPHICAL_MINIMUM_RATIO);
    }
}
