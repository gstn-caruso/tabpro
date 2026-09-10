package com.gstncaruso.tabpro.app;

import com.gstncaruso.tabpro.ui.theme.Contrast;
import org.junit.jupiter.api.Test;

/**
 * Reads the palette directly from {@link Theme}, not through {@code UIManager}, to avoid a race
 * with another test mutating that shared, JVM-wide state.
 */
class ThemeContrastTest {

    @Test
    void theDarkThemeReadsOverItself() {
        PaletteContrastAssertions.assertReads(
                Theme.paletteFor(Theme.DARK), Contrast.TEXT_MINIMUM_RATIO, Contrast.GRAPHICAL_MINIMUM_RATIO);
    }

    @Test
    void theLightThemeReadsOverItself() {
        PaletteContrastAssertions.assertReads(
                Theme.paletteFor(Theme.LIGHT), Contrast.TEXT_MINIMUM_RATIO, Contrast.GRAPHICAL_MINIMUM_RATIO);
    }
}
