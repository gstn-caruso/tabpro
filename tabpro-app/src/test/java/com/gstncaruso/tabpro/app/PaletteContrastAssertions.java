package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.app.Theme.Palette;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * El mismo enumerado de pares de {@link Theme.Palette} que se somete a {@link Contrast.ratio},
 * pero contra los umbrales que le pase cada test: la paleta AA los pide a 4.5/3.0, la de alto
 * contraste a 7.0/4.5.
 */
final class PaletteContrastAssertions {

    private PaletteContrastAssertions() {
    }

    static void assertReads(Palette palette, double textMinimumRatio, double graphicalMinimumRatio) {
        Color accent = palette.accent();
        List<String> failures = new ArrayList<>();
        check(failures, "texto / fondo", palette.text(), palette.background(), textMinimumRatio);
        check(failures, "texto / panel", palette.text(), palette.panel(), textMinimumRatio);
        check(failures, "texto / panel elevado", palette.text(), palette.raisedPanel(), textMinimumRatio);
        check(failures, "texto atenuado / panel", palette.mutedText(), palette.panel(), textMinimumRatio);
        check(failures, "texto atenuado / panel elevado", palette.mutedText(), palette.raisedPanel(),
                textMinimumRatio);
        check(failures, "separador / panel", palette.separator(), palette.panel(), graphicalMinimumRatio);
        check(failures, "separador / fondo", palette.separator(), palette.background(), graphicalMinimumRatio);
        check(failures, "foco / panel", accent, palette.panel(), graphicalMinimumRatio);
        check(failures, "foco / fondo", accent, palette.background(), graphicalMinimumRatio);
        check(failures, "seleccion de menu (texto negro) / foco", Color.BLACK, accent, textMinimumRatio);
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
