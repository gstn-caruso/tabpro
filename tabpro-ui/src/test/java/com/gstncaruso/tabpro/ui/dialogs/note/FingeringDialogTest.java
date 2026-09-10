package com.gstncaruso.tabpro.ui.dialogs.note;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FingeringDialogTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        FingeringDialog.Fields fields = FingeringDialog.buildFields(Optional.empty(), Optional.empty());

        AccessibilityAssertions.assertNoViolations(fields.form());
    }
}
