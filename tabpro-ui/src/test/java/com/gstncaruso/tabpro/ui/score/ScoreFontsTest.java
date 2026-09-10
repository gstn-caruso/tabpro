package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Font;
import org.junit.jupiter.api.Test;

class ScoreFontsTest {

    @Test
    void resolvesTheScoreFamilyAgainstTheInstalledFonts() {
        assertEquals("Liberation Serif", ScoreFonts.FAMILY);
    }

    @Test
    void theTabMarkIsSansSerifNotTheScoreSerif() {
        Font tabMark = ScoreFonts.tabMarkFont(12);
        assertEquals("Liberation Sans", tabMark.getFamily());
        assertNotEquals(ScoreFonts.FAMILY, tabMark.getFamily());
    }
}
