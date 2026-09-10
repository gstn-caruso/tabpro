package com.gstncaruso.tabpro.app;

import com.gstncaruso.tabpro.ui.theme.Contrast;
import org.junit.jupiter.api.Test;

/**
 * Los dos temas de la ventana: oscuro y claro. Lee la paleta directo de {@link Theme}, sin pasar
 * por {@code UIManager}, para no correr una carrera con otro test que tambien lo mute.
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
