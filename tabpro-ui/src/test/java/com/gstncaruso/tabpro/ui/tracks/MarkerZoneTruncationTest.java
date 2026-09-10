package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class MarkerZoneTruncationTest {

    private static final FontMetrics METRICS = metrics();

    @Test
    void keepsAShortNameWhole() {
        assertEquals("Intro", MarkerZone.truncated("Intro", 200, METRICS));
    }

    @Test
    void truncatesANameThatDoesNotFitTheSegment() {
        String truncated = MarkerZone.truncated("Chorus final", 20, METRICS);

        assertTrue(truncated.endsWith("…"), "has to end in an ellipsis: " + truncated);
        assertTrue(METRICS.stringWidth(truncated) <= 20, "has to fit within the segment width");
    }

    private static FontMetrics metrics() {
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        return probe.createGraphics().getFontMetrics(new Font(Font.SANS_SERIF, Font.BOLD, 9));
    }
}
