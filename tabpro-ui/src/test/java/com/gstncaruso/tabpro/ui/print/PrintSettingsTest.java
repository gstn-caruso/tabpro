package com.gstncaruso.tabpro.ui.print;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PrintSettingsTest {

    private static final int SEVEN_SHEETS = 7;
    private static final double EXACTO = 0.0001;

    @Test
    void printingEverythingPrintsEverySheetInOrder() {
        PrintSettings everything = PrintSettings.everything(SEVEN_SHEETS);

        assertEquals(7, everything.sheetsToPrint());
        assertEquals(1, everything.sheetAt(0));
        assertEquals(7, everything.sheetAt(6));
    }

    @Test
    void aScoreOfASingleSheetPrintsThatSheet() {
        PrintSettings everything = PrintSettings.everything(1);

        assertEquals(1, everything.sheetsToPrint());
        assertEquals(1, everything.sheetAt(0));
    }

    @Test
    void aRangePrintsOnlyTheSheetsItAsksFor() {
        PrintSettings fromThreeToFive = PrintSettings.of(3, 5, SEVEN_SHEETS, 100, false);

        assertEquals(3, fromThreeToFive.sheetsToPrint());
        assertEquals(3, fromThreeToFive.sheetAt(0));
        assertEquals(5, fromThreeToFive.sheetAt(2));
    }

    @Test
    void aRangeOfOneSheetPrintsThatOne() {
        PrintSettings onlyTheFourth = PrintSettings.of(4, 4, SEVEN_SHEETS, 100, false);

        assertEquals(1, onlyTheFourth.sheetsToPrint());
        assertEquals(4, onlyTheFourth.sheetAt(0));
    }

    @Test
    void aRangeThatFallsOffTheScoreIsBroughtBackIn() {
        PrintSettings tooWide = PrintSettings.of(0, 99, SEVEN_SHEETS, 100, false);

        assertEquals(1, tooWide.fromSheet());
        assertEquals(SEVEN_SHEETS, tooWide.toSheet());
    }

    @Test
    void aRangeWrittenBackwardsPrintsTheSingleSheetItStartsAt() {
        PrintSettings backwards = PrintSettings.of(5, 2, SEVEN_SHEETS, 100, false);

        assertEquals(1, backwards.sheetsToPrint());
        assertEquals(5, backwards.sheetAt(0));
    }

    @Test
    void theScaleIsTheOneAsked() {
        PrintSettings half = PrintSettings.of(1, 7, SEVEN_SHEETS, 50, false);

        assertEquals(0.5, half.scaleFor(1000, 2000, 500, 500), EXACTO);
    }

    @Test
    void fitToPageShrinksUntilTheWholeSheetFits() {
        PrintSettings fitted = PrintSettings.of(1, 7, SEVEN_SHEETS, 100, true);

        assertEquals(0.25, fitted.scaleFor(1000, 2000, 500, 500), EXACTO);
    }

    @Test
    void fitToPageAlsoGrowsASheetThatIsSmallerThanThePaper() {
        PrintSettings fitted = PrintSettings.of(1, 7, SEVEN_SHEETS, 100, true);

        assertEquals(2.0, fitted.scaleFor(100, 100, 200, 400), EXACTO);
    }

    @Test
    void aScaleOutsideWhatAPrinterCanDoIsBroughtBackIn() {
        assertEquals(
                PrintSettings.MIN_SCALE_PERCENT,
                PrintSettings.of(1, 7, SEVEN_SHEETS, 0, false).scalePercent());
        assertEquals(
                PrintSettings.MAX_SCALE_PERCENT,
                PrintSettings.of(1, 7, SEVEN_SHEETS, 5000, false).scalePercent());
    }

    @Test
    void aScoreWithoutSheetsStillPrintsOne() {
        PrintSettings nothing = PrintSettings.everything(0);

        assertEquals(1, nothing.sheetsToPrint());
        assertEquals(1, nothing.sheetAt(0));
    }
}
