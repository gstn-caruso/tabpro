package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.wizards.BarDurationCheck;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class BarDurationCheckDialogTest {

    @Test
    void theTitleAndAllBarsCompleteMessageAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Check Bar Duration", english.text("score_dialogs.BarDurationCheckDialog.title"));
        assertEquals(
                "All bars add up to their time signature.",
                english.text("score_dialogs.BarDurationCheckDialog.allBarsComplete"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltipWithFindings() {
        List<BarDurationCheck.Finding> findings = List.of(new BarDurationCheck.Finding(0, 0, true));

        AccessibilityAssertions.assertNoViolations(BarDurationCheckDialog.buildContent(findings));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltipWithoutFindings() {
        AccessibilityAssertions.assertNoViolations(BarDurationCheckDialog.buildContent(List.of()));
    }
}
