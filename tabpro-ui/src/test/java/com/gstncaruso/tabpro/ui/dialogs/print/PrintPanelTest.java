package com.gstncaruso.tabpro.ui.dialogs.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.ui.print.PrintSettings;
import org.junit.jupiter.api.Test;

class PrintPanelTest {

    private static final int SEVEN_SHEETS = 7;

    @Test
    void startsPrintingTheWholeScore() {
        PrintSettings settings = new PrintPanel(SEVEN_SHEETS).toPrintSettings();

        assertEquals(1, settings.fromSheet());
        assertEquals(SEVEN_SHEETS, settings.toSheet());
        assertEquals(SEVEN_SHEETS, settings.sheetsToPrint());
    }

    @Test
    void aRangePrintsOnlyThoseSheets() {
        PrintPanel panel = new PrintPanel(SEVEN_SHEETS);

        panel.printOnly(2, 4);

        assertEquals(3, panel.toPrintSettings().sheetsToPrint());
        assertEquals(2, panel.toPrintSettings().sheetAt(0));
    }

    @Test
    void theRangeCannotGoPastTheLastSheet() {
        PrintPanel panel = new PrintPanel(SEVEN_SHEETS);

        panel.printOnly(1, 99);

        assertEquals(SEVEN_SHEETS, panel.toPrintSettings().toSheet());
    }

    @Test
    void theScaleTravelsToTheSettings() {
        PrintPanel panel = new PrintPanel(SEVEN_SHEETS);

        panel.scaleTo(75);

        assertEquals(75, panel.toPrintSettings().scalePercent());
        assertFalse(panel.toPrintSettings().fitToPage());
    }

    @Test
    void fitToPageWorksOutTheScaleSoTheFieldStopsBeingEditable() {
        PrintPanel panel = new PrintPanel(SEVEN_SHEETS);

        panel.fitToPage();

        assertTrue(panel.toPrintSettings().fitToPage());
        assertFalse(panel.scaleIsEditable());
    }

    @Test
    void theRangeIsOnlyEditableWhenARangeWasAskedFor() {
        PrintPanel panel = new PrintPanel(SEVEN_SHEETS);

        assertFalse(panel.rangeIsEditable());

        panel.printOnly(2, 3);

        assertTrue(panel.rangeIsEditable());
    }

    @Test
    void aScoreOfASingleSheetPrintsThatOne() {
        PrintSettings settings = new PrintPanel(1).toPrintSettings();

        assertEquals(1, settings.sheetsToPrint());
    }
}
