package com.gstncaruso.tabpro.ui.dialogs.tuner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.dialogs.RecordingPlayer;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TunerDialogTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        Editor editor = new Editor(Score.blank());

        AccessibilityAssertions.assertNoViolations(
                TunerDialog.buildTabs(editor, new RecordingPlayer(), Ports.Microphone.NONE).pane());
    }

    @Test
    void theTitleAndStringToTuneLabelAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Tuner", english.text("score_dialogs.TunerDialog.title"));
        assertEquals("String to Tune", english.text("score_dialogs.TunerDialog.stringToTune"));
    }
}
