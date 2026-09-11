package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class BarArrangerDialogTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        BarArrangerDialog.Fields fields = BarArrangerDialog.buildFields();

        AccessibilityAssertions.assertNoViolations(fields.content());
    }

    @Test
    void theTitleAndAcceptButtonAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Bar Arranger", english.text("score_dialogs.BarArrangerDialog.title"));
        assertEquals("Arrange", english.text("score_dialogs.BarArrangerDialog.accept"));
    }
}
