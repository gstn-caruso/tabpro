package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/** Como en Guitar Pro 5: el nombre del marcador se corta con puntos suspensivos si no entra. */
class MarkerZoneTruncationTest {

    private static final FontMetrics METRICS = metrics();

    @Test
    void keepsAShortNameWhole() {
        assertEquals("Intro", MarkerZone.truncated("Intro", 200, METRICS));
    }

    @Test
    void truncatesANameThatDoesNotFitTheSegment() {
        String truncated = MarkerZone.truncated("Estribillo final", 20, METRICS);

        assertTrue(truncated.endsWith("…"), "tiene que terminar en puntos suspensivos: " + truncated);
        assertTrue(METRICS.stringWidth(truncated) <= 20, "tiene que entrar en el ancho del segmento");
    }

    private static FontMetrics metrics() {
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        return probe.createGraphics().getFontMetrics(new Font(Font.SANS_SERIF, Font.BOLD, 9));
    }
}
