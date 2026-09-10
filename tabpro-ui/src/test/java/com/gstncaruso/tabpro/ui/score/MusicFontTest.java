package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Font;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * Guitar Pro 5 graba clave y cifra de compas con Bravura (SMuFL) en vez de dibujarlas a mano.
 * La fuente viaja en el jar como recurso: un empaquetado roto tiene que fallar aca, no en
 * produccion.
 */
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
}
