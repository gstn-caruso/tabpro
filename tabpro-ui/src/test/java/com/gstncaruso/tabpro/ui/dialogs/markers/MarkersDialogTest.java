package com.gstncaruso.tabpro.ui.dialogs.markers;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class MarkersDialogTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        Editor editor = new Editor(Score.blank());

        AccessibilityAssertions.assertNoViolations(MarkersDialog.buildContent(editor));
    }
}
