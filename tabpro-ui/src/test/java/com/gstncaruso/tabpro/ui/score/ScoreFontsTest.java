package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Font;
import org.junit.jupiter.api.Test;

/**
 * Guitar Pro 5 escribe la partitura en una serif tipo Times New Roman; en Linux esa familia no
 * esta instalada y hay que resolver contra las que si lo estan.
 */
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
