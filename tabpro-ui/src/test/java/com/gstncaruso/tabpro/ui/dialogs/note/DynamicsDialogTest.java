package com.gstncaruso.tabpro.ui.dialogs.note;

import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class DynamicsDialogTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        DynamicsDialog.Fields fields = DynamicsDialog.buildFields(Dynamic.defaultDynamic());

        AccessibilityAssertions.assertNoViolations(fields.form());
    }
}
