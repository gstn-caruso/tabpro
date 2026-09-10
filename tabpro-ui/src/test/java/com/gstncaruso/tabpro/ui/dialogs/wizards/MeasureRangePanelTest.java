package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.wizards.MeasureRange;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class MeasureRangePanelTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        AccessibilityAssertions.assertNoViolations(new MeasureRangePanel(10));
    }

    @Test
    void defaultsToTheWholeScore() {
        MeasureRangePanel panel = new MeasureRangePanel(10);

        assertEquals(MeasureRange.wholeScore(10), panel.toMeasureRange());
    }

    @Test
    void startsWithTheGivenRange() {
        MeasureRangePanel panel = new MeasureRangePanel(new MeasureRange(3, 5), 10);

        assertEquals(new MeasureRange(3, 5), panel.toMeasureRange());
    }
}
