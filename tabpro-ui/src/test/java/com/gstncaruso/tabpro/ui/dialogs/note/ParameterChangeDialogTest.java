package com.gstncaruso.tabpro.ui.dialogs.note;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class ParameterChangeDialogTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        Editor editor = new Editor(Score.blank());

        ParameterChangeDialog.Fields fields = ParameterChangeDialog.buildFields(ParameterChange.nothing(), editor);

        AccessibilityAssertions.assertNoViolations(fields.form());
    }
}
