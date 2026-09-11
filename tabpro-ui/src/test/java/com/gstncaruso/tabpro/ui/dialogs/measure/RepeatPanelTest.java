package com.gstncaruso.tabpro.ui.dialogs.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class RepeatPanelTest {

    @Test
    void theRepeatOpenAndRepeatCountFieldsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Repeat Open", english.text("edit_dialogs.RepeatPanel.repeatOpen"));
        assertEquals("Close After This Many Times (0 = Never)", english.text("edit_dialogs.RepeatPanel.repeatCount"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new RepeatPanel(false, 0));
    }

    @Test
    void startsWithTheGivenValues() {
        RepeatPanel panel = new RepeatPanel(true, 3);

        assertTrue(panel.repeatOpenSelected());
        assertEquals(3, panel.toRepeatCount());
    }

    @Test
    void doesNotReportAChangeWhenNothingMoved() {
        RepeatPanel panel = new RepeatPanel(true, 3);

        assertFalse(panel.repeatOpenChanged());
    }

    @Test
    void reportsAChangeOnlyWhenTheCheckboxMoved() {
        RepeatPanel panel = new RepeatPanel(false, 0);

        assertFalse(panel.repeatOpenChanged());

        panel.setRepeatOpenSelected(true);

        assertTrue(panel.repeatOpenChanged());
    }
}
