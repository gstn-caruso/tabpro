package com.gstncaruso.tabpro.ui.dialogs.ascii;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AsciiExportPanelTest {

    @Test
    void defaultsToTheStandardColumnCount() {
        AsciiExportPanel panel = new AsciiExportPanel();

        assertEquals(80, panel.columnsPerLine());
    }

    @Test
    void changingTheColumnsUpdatesTheValue() {
        AsciiExportPanel panel = new AsciiExportPanel();

        panel.setColumnsPerLine(40);

        assertEquals(40, panel.columnsPerLine());
    }

    @Test
    void showsThePreviewTextThatIsGiven() {
        AsciiExportPanel panel = new AsciiExportPanel();

        panel.showPreview("Guitarra\n|-5-|");

        assertEquals("Guitarra\n|-5-|", panel.previewText());
    }
}
