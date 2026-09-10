package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import org.junit.jupiter.api.Test;

class TabFretDigitProportionTest {

    @Test
    void theFretDigitFillsAsMuchOfTheStringSpacingAsInGuitarPro5() {
        FontRenderContext context = new FontRenderContext(null, true, true);
        GlyphVector glyphs = ScoreFonts.FRET_FONT.createGlyphVector(context, "0357");
        double digitHeight = glyphs.getVisualBounds().getHeight();
        double ratio = digitHeight / ScoreLayout.STRING_SPACING;

        assertTrue(ratio >= 0.85 && ratio <= 0.92, "ratio was " + ratio);
    }
}
