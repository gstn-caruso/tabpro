package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Font;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class MusicFontTest {

    @Test
    void theBravuraResourceLoadsFromTheClasspathAsAFont() throws Exception {
        try (InputStream resource = MusicFontTest.class.getResourceAsStream("/fonts/Bravura.otf")) {
            assertNotNull(resource, "Bravura.otf tiene que viajar en el classpath");
            Font bravura = Font.createFont(Font.TRUETYPE_FONT, resource);
            assertEquals("Bravura", bravura.getFamily());
        }
    }

    @Test
    void trebleClefIsTheGClefGlyph() {
        assertEquals(0xE050, MusicFont.trebleClef().codePointAt(0));
    }

    @Test
    void bassClefIsTheFClefGlyph() {
        assertEquals(0xE062, MusicFont.bassClef().codePointAt(0));
    }

    @Test
    void timeSignatureDigitReturnsTheMatchingGlyphForEachDigit() {
        for (int digit = 0; digit <= 9; digit++) {
            assertEquals(0xE080 + digit, MusicFont.timeSignatureDigit(digit).codePointAt(0));
        }
    }

    @Test
    void theSizedFontFollowsTheSmuflEmSquareConvention() {
        Font sized = MusicFont.sizedTo(8);
        assertEquals("Bravura", sized.getFamily());
        assertEquals(32f, sized.getSize2D());
    }

    @Test
    void theSizedFontIsCachedPerSize() {
        assertSame(MusicFont.sizedTo(8), MusicFont.sizedTo(8));
    }

    @Test
    void noteheadBlackIsTheFilledNoteheadGlyph() {
        assertEquals(0xE0A4, MusicFont.noteheadBlack().codePointAt(0));
    }

    @Test
    void noteheadHalfIsTheHollowHalfNoteheadGlyph() {
        assertEquals(0xE0A3, MusicFont.noteheadHalf().codePointAt(0));
    }

    @Test
    void noteheadWholeIsTheHollowWholeNoteheadGlyph() {
        assertEquals(0xE0A2, MusicFont.noteheadWhole().codePointAt(0));
    }

    @Test
    void accidentalSharpIsTheSharpGlyph() {
        assertEquals(0xE262, MusicFont.accidentalSharp().codePointAt(0));
    }

    @Test
    void accidentalFlatIsTheFlatGlyph() {
        assertEquals(0xE260, MusicFont.accidentalFlat().codePointAt(0));
    }

    @Test
    void accidentalNaturalIsTheNaturalGlyph() {
        assertEquals(0xE261, MusicFont.accidentalNatural().codePointAt(0));
    }

    @Test
    void augmentationDotIsTheAugmentationDotGlyph() {
        assertEquals(0xE1E7, MusicFont.augmentationDot().codePointAt(0));
    }

    @Test
    void restWholeIsTheWholeRestGlyph() {
        assertEquals(0xE4E3, MusicFont.restWhole().codePointAt(0));
    }

    @Test
    void restHalfIsTheHalfRestGlyph() {
        assertEquals(0xE4E4, MusicFont.restHalf().codePointAt(0));
    }

    @Test
    void restQuarterIsTheQuarterRestGlyph() {
        assertEquals(0xE4E5, MusicFont.restQuarter().codePointAt(0));
    }

    @Test
    void rest8thIsTheEighthRestGlyph() {
        assertEquals(0xE4E6, MusicFont.rest8th().codePointAt(0));
    }

    @Test
    void rest16thIsTheSixteenthRestGlyph() {
        assertEquals(0xE4E7, MusicFont.rest16th().codePointAt(0));
    }
}
