package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.gstncaruso.tabpro.core.editing.wizards.MeasureRange;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class RestFillerPanelTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new RestFillerPanel(8));
    }

    @Test
    void defaultsToTheWholeCurrentTrack() {
        RestFillerPanel panel = new RestFillerPanel(8);

        assertEquals(MeasureRange.wholeScore(8), panel.toMeasureRange());
        assertFalse(panel.everyTrack());
    }
}
