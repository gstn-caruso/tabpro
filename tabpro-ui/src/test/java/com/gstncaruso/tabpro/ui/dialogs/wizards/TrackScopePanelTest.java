package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TrackScopePanelTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new TrackScopePanel());
    }

    @Test
    void theTrackScopeOptionsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Current Track", english.text("score_dialogs.TrackScopePanel.currentTrack"));
        assertEquals("Every Track", english.text("score_dialogs.TrackScopePanel.everyTrack"));
    }
}
