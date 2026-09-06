package com.gstncaruso.tabpro.ui.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PageMetricsTest {

    @Test
    void aLetterSheetIsEightHundredAndFiftyPixelsByAThousandOneHundred() {
        PageMetrics sheet = PageMetrics.of(setupOn(PaperFormat.LETTER, Orientation.PORTRAIT));

        assertEquals(850, sheet.pageWidth());
        assertEquals(1100, sheet.pageHeight());
    }

    @Test
    void turningTheSheetSidewaysSwapsItsSides() {
        PageMetrics portrait = PageMetrics.of(setupOn(PaperFormat.A4, Orientation.PORTRAIT));
        PageMetrics landscape = PageMetrics.of(setupOn(PaperFormat.A4, Orientation.LANDSCAPE));

        assertEquals(portrait.pageHeight(), landscape.pageWidth());
        assertEquals(portrait.pageWidth(), landscape.pageHeight());
    }

    @Test
    void aBiggerFormatMakesABiggerSheet() {
        PageMetrics a4 = PageMetrics.of(setupOn(PaperFormat.A4, Orientation.PORTRAIT));
        PageMetrics a3 = PageMetrics.of(setupOn(PaperFormat.A3, Orientation.PORTRAIT));

        assertTrue(a3.pageWidth() > a4.pageWidth());
        assertTrue(a3.pageHeight() > a4.pageHeight());
    }

    @Test
    void withoutMarginsTheContentIsAsWideAsTheSheet() {
        PageMetrics sheet = PageMetrics.of(withMargins(0, 0, 0, 0));

        assertEquals(0, sheet.contentLeft());
        assertEquals(sheet.pageWidth(), sheet.contentWidth());
    }

    @Test
    void theMarginsEatIntoTheContent() {
        PageMetrics sheet = PageMetrics.of(withMargins(10, 20, 30, 40));

        assertEquals(PageMetrics.pixelsOf(30), sheet.contentLeft());
        assertEquals(sheet.pageWidth() - PageMetrics.pixelsOf(30) - PageMetrics.pixelsOf(40), sheet.contentWidth());
        assertEquals(PageMetrics.pixelsOf(10) + PageMetrics.HEADER_HEIGHT, sheet.contentTop());
    }

    @Test
    void theHeaderAndTheFooterAlsoEatIntoTheContent() {
        PageMetrics sheet = PageMetrics.of(withMargins(0, 0, 0, 0));

        assertEquals(sheet.pageHeight() - PageMetrics.HEADER_HEIGHT - PageMetrics.FOOTER_HEIGHT, sheet.contentHeight());
    }

    @Test
    void marginsWiderThanTheSheetStillLeaveRoomToDrawSomething() {
        PageMetrics sheet = PageMetrics.of(withMargins(500, 500, 500, 500));

        assertTrue(sheet.contentWidth() > 0, "el ancho del contenido nunca puede ser negativo");
        assertTrue(sheet.contentHeight() > 0, "el alto del contenido nunca puede ser negativo");
    }

    @Test
    void atFullSizeTheScoreIsDrawnOneToOne() {
        PageMetrics sheet = PageMetrics.of(sized(100));

        assertEquals(1.0, sheet.scoreScale(), 0.0001);
        assertEquals(sheet.contentWidth(), sheet.layoutWidth());
    }

    @Test
    void atHalfSizeTwiceAsMuchMusicFitsOnTheSheet() {
        PageMetrics sheet = PageMetrics.of(sized(50));

        assertEquals(0.5, sheet.scoreScale(), 0.0001);
        assertEquals(2 * sheet.contentWidth(), sheet.layoutWidth());
        assertEquals(2 * sheet.contentHeight(), sheet.layoutHeight());
    }

    @Test
    void aBiggerScorePercentDoesNotChangeThePaper() {
        PageMetrics small = PageMetrics.of(sized(50));
        PageMetrics big = PageMetrics.of(sized(200));

        assertEquals(small.pageWidth(), big.pageWidth());
        assertEquals(small.pageHeight(), big.pageHeight());
        assertTrue(big.layoutWidth() < small.layoutWidth(), "agrandar la partitura deja entrar menos musica");
    }

    private static PageSetup setupOn(PaperFormat format, Orientation orientation) {
        return new PageSetup(format, orientation, 20, 20, 20, 20, 100, "", "");
    }

    private static PageSetup withMargins(int top, int bottom, int left, int right) {
        return new PageSetup(
                PaperFormat.A4, Orientation.PORTRAIT, top, bottom, left, right, 100, "", "");
    }

    private static PageSetup sized(int scorePercent) {
        return new PageSetup(
                PaperFormat.A4, Orientation.PORTRAIT, 20, 20, 20, 20, scorePercent, "", "");
    }
}
