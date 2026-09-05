package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.gstncaruso.tabpro.core.editing.wizards.MeasureRange;
import org.junit.jupiter.api.Test;

class RestFillerPanelTest {

    @Test
    void defaultsToTheWholeCurrentTrack() {
        RestFillerPanel panel = new RestFillerPanel(8);

        assertEquals(MeasureRange.wholeScore(8), panel.toMeasureRange());
        assertFalse(panel.everyTrack());
    }
}
