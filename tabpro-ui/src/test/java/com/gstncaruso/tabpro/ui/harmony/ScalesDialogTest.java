package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class ScalesDialogTest {

    @Test
    void ningunControlQuedaSinNombreNiTooltipAccesible() {
        Editor editor = new Editor(Score.blank());

        ScalesDialog.Panel panel = new ScalesDialog.Panel(editor, new RecordingPlayer(), new ChosenScale());

        AccessibilityAssertions.assertNoViolations(panel);
    }
}
