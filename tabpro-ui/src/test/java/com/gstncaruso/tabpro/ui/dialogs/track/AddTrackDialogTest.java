package com.gstncaruso.tabpro.ui.dialogs.track;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class AddTrackDialogTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        AddTrackDialog.Fields fields = AddTrackDialog.buildFields(1);

        AccessibilityAssertions.assertNoViolations(fields.form());
    }
}
