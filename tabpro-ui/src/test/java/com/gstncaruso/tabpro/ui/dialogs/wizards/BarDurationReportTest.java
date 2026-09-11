package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.wizards.BarDurationCheck;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class BarDurationReportTest {

    @Test
    void theTooShortAndTooLongDescriptionsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("missing beats", english.text("score_dialogs.BarDurationReport.tooShort"));
        assertEquals("extra beats", english.text("score_dialogs.BarDurationReport.tooLong"));
        assertEquals("Track 1, bar 3: missing beats", english.text("score_dialogs.BarDurationReport.finding", 1, 3, "missing beats"));
    }

    @Test
    void describesATooShortMeasure() {
        BarDurationCheck.Finding finding = new BarDurationCheck.Finding(0, 2, true);

        String description = BarDurationReport.describe(finding);

        assertEquals("Pista 1, compás 3: le faltan pulsos", description);
    }

    @Test
    void describesATooLongMeasure() {
        BarDurationCheck.Finding finding = new BarDurationCheck.Finding(1, 0, false);

        String description = BarDurationReport.describe(finding);

        assertTrue(description.contains("le sobran pulsos"));
        assertTrue(description.contains("Pista 2"));
    }
}
