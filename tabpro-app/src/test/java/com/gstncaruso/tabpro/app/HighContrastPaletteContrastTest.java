package com.gstncaruso.tabpro.app;

import org.junit.jupiter.api.Test;

class HighContrastPaletteContrastTest {

    private static final double TEXT_MINIMUM_RATIO = 7.0;
    private static final double GRAPHICAL_MINIMUM_RATIO = 4.5;

    @Test
    void theHighContrastPaletteReadsOverItself() {
        PaletteContrastAssertions.assertReads(Theme.highContrastPalette(), TEXT_MINIMUM_RATIO, GRAPHICAL_MINIMUM_RATIO);
    }
}
