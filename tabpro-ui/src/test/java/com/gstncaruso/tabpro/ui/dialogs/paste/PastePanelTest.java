package com.gstncaruso.tabpro.ui.dialogs.paste;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.PasteOptions;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class PastePanelTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new PastePanel());
    }

    @Test
    void defaultsToInsertingOnce() {
        PastePanel panel = new PastePanel();

        assertEquals(PasteOptions.insertingOnce(), panel.toPasteOptions());
    }

    @Test
    void canBeSetToReplaceSeveralTimes() {
        PastePanel panel = new PastePanel();

        panel.selectReplacing();
        panel.setRepetitions(4);

        assertEquals(new PasteOptions(false, 4), panel.toPasteOptions());
    }
}
