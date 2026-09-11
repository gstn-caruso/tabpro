package com.gstncaruso.tabpro.ui.dialogs.note;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class SoundDurationDialogTest {

    @Test
    void theTitleAndPercentFieldAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Sound Duration", english.text("edit_dialogs.SoundDurationDialog.title"));
        assertEquals("Sound Duration (%)", english.text("edit_dialogs.SoundDurationDialog.percent"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        SoundDurationDialog.Fields fields = SoundDurationDialog.buildFields(NoteEffects.FULL_SOUND);

        AccessibilityAssertions.assertNoViolations(fields.form());
    }
}
