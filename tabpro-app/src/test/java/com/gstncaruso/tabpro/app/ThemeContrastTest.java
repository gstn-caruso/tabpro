package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.app.Theme.Palette;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Los dos temas de la ventana: oscuro y claro. Lee la paleta directo de {@link Theme}, sin pasar
 * por {@code UIManager}, para no correr una carrera con otro test que tambien lo mute.
 */
class ThemeContrastTest {

    @Test
    void theDarkThemeReadsOverItself() {
        assertReads(Theme.paletteFor(Theme.DARK));
    }

    @Test
    void theLightThemeReadsOverItself() {
        assertReads(Theme.paletteFor(Theme.LIGHT));
    }

    private static void assertReads(Palette palette) {
        Color accent = palette.accent();
        List<String> failures = new ArrayList<>();
        check(failures, "texto / fondo", palette.text(), palette.background(), Contrast.TEXT_MINIMUM_RATIO);
        check(failures, "texto / panel", palette.text(), palette.panel(), Contrast.TEXT_MINIMUM_RATIO);
        check(failures, "texto / panel elevado", palette.text(), palette.raisedPanel(), Contrast.TEXT_MINIMUM_RATIO);
        check(failures, "texto atenuado / panel", palette.mutedText(), palette.panel(), Contrast.TEXT_MINIMUM_RATIO);
        check(failures, "texto atenuado / panel elevado", palette.mutedText(), palette.raisedPanel(),
                Contrast.TEXT_MINIMUM_RATIO);
        check(failures, "separador / panel", palette.separator(), palette.panel(),
                Contrast.GRAPHICAL_MINIMUM_RATIO);
        check(failures, "separador / fondo", palette.separator(), palette.background(),
                Contrast.GRAPHICAL_MINIMUM_RATIO);
        check(failures, "foco / panel", accent, palette.panel(), Contrast.GRAPHICAL_MINIMUM_RATIO);
        check(failures, "foco / fondo", accent, palette.background(), Contrast.GRAPHICAL_MINIMUM_RATIO);
        check(failures, "seleccion de menu (texto negro) / foco", Color.BLACK, accent, Contrast.TEXT_MINIMUM_RATIO);
        assertTrue(
                failures.isEmpty(),
                () -> failures.size() + " par(es) no llegan al contraste minimo:\n" + String.join("\n", failures));
    }

    private static void check(List<String> failures, String description, Color foreground, Color background,
            double minimumRatio) {
        double ratio = Contrast.ratio(foreground, background);
        if (ratio < minimumRatio) {
            failures.add(String.format("%s: %.2f (necesita >= %.1f)", description, ratio, minimumRatio));
        }
    }
}
