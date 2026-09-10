package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.core.editing.wizards.BarDurationCheck;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.util.List;
import org.junit.jupiter.api.Test;

class BarDurationCheckDialogTest {

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
