package com.gstncaruso.tabpro.ui.dialogs.ascii;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class AsciiExportPanelTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new AsciiExportPanel());
    }

    @Test
    void theColumnsPerLineLabelAndExportButtonAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Columns per Line", english.text("score_dialogs.AsciiExportPanel.columnsPerLine"));
        assertEquals("Export…", english.text("score_dialogs.AsciiExportPanel.export"));
    }

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
