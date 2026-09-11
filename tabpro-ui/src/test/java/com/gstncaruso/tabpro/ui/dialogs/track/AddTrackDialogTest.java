package com.gstncaruso.tabpro.ui.dialogs.track;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class AddTrackDialogTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AddTrackDialog.Fields fields = AddTrackDialog.buildFields(1);

        AccessibilityAssertions.assertNoViolations(fields.form());
    }

    @Test
    void theTitleAndDefaultTrackNameAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Add a Track", english.text("score_dialogs.shared.addTrack"));
        assertEquals("Track 3", english.text("score_dialogs.AddTrackDialog.defaultName", 3));
    }
}
