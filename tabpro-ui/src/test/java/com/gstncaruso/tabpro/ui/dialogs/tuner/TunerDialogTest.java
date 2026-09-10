package com.gstncaruso.tabpro.ui.dialogs.tuner;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.dialogs.RecordingPlayer;
import org.junit.jupiter.api.Test;

class TunerDialogTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        Editor editor = new Editor(Score.blank());

        AccessibilityAssertions.assertNoViolations(
                TunerDialog.buildTabs(editor, new RecordingPlayer(), Ports.Microphone.NONE).pane());
    }
}
