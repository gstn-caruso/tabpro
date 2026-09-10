package com.gstncaruso.tabpro.ui.dialogs.note;

import com.gstncaruso.tabpro.core.model.effects.NoteEffects;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import org.junit.jupiter.api.Test;

class SoundDurationDialogTest {

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        SoundDurationDialog.Fields fields = SoundDurationDialog.buildFields(NoteEffects.FULL_SOUND);

        AccessibilityAssertions.assertNoViolations(fields.form());
    }
}
