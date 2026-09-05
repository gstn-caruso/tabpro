package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.wizards.BarDurationCheck;
import org.junit.jupiter.api.Test;

class BarDurationReportTest {

    @Test
    void describesATooShortMeasure() {
        BarDurationCheck.Finding finding = new BarDurationCheck.Finding(0, 2, true);

        String description = BarDurationReport.describe(finding);

        assertEquals("Pista 1, compas 3: le faltan pulsos", description);
    }

    @Test
    void describesATooLongMeasure() {
        BarDurationCheck.Finding finding = new BarDurationCheck.Finding(1, 0, false);

        String description = BarDurationReport.describe(finding);

        assertTrue(description.contains("le sobran pulsos"));
        assertTrue(description.contains("Pista 2"));
    }
}
