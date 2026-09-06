package com.gstncaruso.tabpro.ui.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PaperFormatTest {

    private static final double A_TENTH_OF_A_MILLIMETRE = 0.1;

    @Test
    void anA4SheetIsTwoHundredAndTenMillimetresByTwoHundredAndNinetySeven() {
        assertEquals(210, PaperFormat.A4.widthMillimetres(), A_TENTH_OF_A_MILLIMETRE);
        assertEquals(297, PaperFormat.A4.heightMillimetres(), A_TENTH_OF_A_MILLIMETRE);
    }

    @Test
    void aLetterSheetIsEightAndAHalfInchesByEleven() {
        assertEquals(8.5 * 25.4, PaperFormat.LETTER.widthMillimetres(), A_TENTH_OF_A_MILLIMETRE);
        assertEquals(11 * 25.4, PaperFormat.LETTER.heightMillimetres(), A_TENTH_OF_A_MILLIMETRE);
    }

    @Test
    void anA3SheetIsAnA4TurnedSidewaysAndDoubled() {
        assertEquals(PaperFormat.A4.heightMillimetres(), PaperFormat.A3.widthMillimetres(), A_TENTH_OF_A_MILLIMETRE);
        assertEquals(2 * PaperFormat.A4.widthMillimetres(), PaperFormat.A3.heightMillimetres(), 1.0);
    }

    @Test
    void aLegalSheetIsAsWideAsALetterButLonger() {
        assertEquals(PaperFormat.LETTER.widthMillimetres(), PaperFormat.LEGAL.widthMillimetres(), A_TENTH_OF_A_MILLIMETRE);
        assertTrue(PaperFormat.LEGAL.heightMillimetres() > PaperFormat.LETTER.heightMillimetres());
    }

    @Test
    void everyFormatIsTallerThanItIsWide() {
        for (PaperFormat format : PaperFormat.values()) {
            assertTrue(format.heightMillimetres() > format.widthMillimetres(), format + " se mide en vertical");
        }
    }
}
