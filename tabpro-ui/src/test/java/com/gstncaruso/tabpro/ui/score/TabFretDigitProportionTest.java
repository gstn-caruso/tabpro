package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import org.junit.jupiter.api.Test;

/**
 * Guitar Pro 5 mide el digito de traste con un bounding box de 5x8 px sobre un espaciado entre
 * cuerdas de 9 px: el digito ocupa 0,89 de ese espaciado.
 */
class TabFretDigitProportionTest {

    @Test
    void theFretDigitFillsAsMuchOfTheStringSpacingAsInGuitarPro5() {
        FontRenderContext context = new FontRenderContext(null, true, true);
        GlyphVector glyphs = ScoreFonts.FRET_FONT.createGlyphVector(context, "0357");
        double digitHeight = glyphs.getVisualBounds().getHeight();
        double ratio = digitHeight / ScoreLayout.STRING_SPACING;

        assertTrue(ratio >= 0.85 && ratio <= 0.92, "ratio fue " + ratio);
    }
}
