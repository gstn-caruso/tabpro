package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TranspositionPanelTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new TranspositionPanel());
    }

    @Test
    void theSemitonesLabelIsAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Semitones", english.text("score_dialogs.TranspositionPanel.semitones"));
    }

    @Test
    void defaultsToNoTranspositionOnTheCurrentTrack() {
        TranspositionPanel panel = new TranspositionPanel();

        assertEquals(0, panel.semitones());
        assertFalse(panel.everyTrack());
    }
}
