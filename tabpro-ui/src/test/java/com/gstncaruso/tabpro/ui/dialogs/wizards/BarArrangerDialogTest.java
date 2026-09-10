package com.gstncaruso.tabpro.ui.dialogs.wizards;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class BarArrangerDialogTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        BarArrangerDialog.Fields fields = BarArrangerDialog.buildFields();

        AccessibilityAssertions.assertNoViolations(fields.content());
    }
}
