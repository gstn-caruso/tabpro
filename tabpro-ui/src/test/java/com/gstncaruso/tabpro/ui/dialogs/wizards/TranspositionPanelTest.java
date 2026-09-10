package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class TranspositionPanelTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        AccessibilityAssertions.assertNoViolations(new TranspositionPanel());
    }

    @Test
    void defaultsToNoTranspositionOnTheCurrentTrack() {
        TranspositionPanel panel = new TranspositionPanel();

        assertEquals(0, panel.semitones());
        assertFalse(panel.everyTrack());
    }
}
