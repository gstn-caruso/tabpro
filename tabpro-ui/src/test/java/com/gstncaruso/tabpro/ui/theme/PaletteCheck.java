package com.gstncaruso.tabpro.ui.theme;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Un par de colores de la paleta: lo que se pinta, sobre que fondo, y el contraste minimo que
 * WCAG le exige (texto normal o un componente/trazo con significado).
 */
public final class PaletteCheck {

    private PaletteCheck() {
    }

    public record Pair(String description, Color foreground, Color background, double minimumRatio) {
    }

    public static void assertEveryPairReads(List<Pair> pairs) {
        List<String> failures = new ArrayList<>();
        for (Pair pair : pairs) {
            double ratio = Contrast.ratio(pair.foreground(), pair.background());
            if (ratio < pair.minimumRatio()) {
                failures.add(String.format(
                        "%s: %.2f (necesita >= %.1f)", pair.description(), ratio, pair.minimumRatio()));
            }
        }
        assertTrue(
                failures.isEmpty(),
                () -> failures.size() + " par(es) no llegan al contraste minimo:\n" + String.join("\n", failures));
    }
}
