package com.gstncaruso.tabpro.ui.dialogs.note;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ParameterChangeDialogTest {

    @Test
    void theTitleAndEveryTrackFieldAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Parameter Change", english.text("edit_dialogs.ParameterChangeDialog.title"));
        assertEquals("Apply to All Tracks", english.text("edit_dialogs.ParameterChangeDialog.everyTrack"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        Editor editor = new Editor(Score.blank());

        ParameterChangeDialog.Fields fields = ParameterChangeDialog.buildFields(ParameterChange.nothing(), editor);

        AccessibilityAssertions.assertNoViolations(fields.form());
    }
}
