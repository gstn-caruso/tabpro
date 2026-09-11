package com.gstncaruso.tabpro.ui.dialogs.note;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FingeringDialogTest {

    @Test
    void theTitleAndLeftHandFieldAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Fingering", english.text("edit_dialogs.FingeringDialog.title"));
        assertEquals("Left Hand", english.text("edit_dialogs.FingeringDialog.leftHand"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        FingeringDialog.Fields fields = FingeringDialog.buildFields(Optional.empty(), Optional.empty());

        AccessibilityAssertions.assertNoViolations(fields.form());
    }

    @Test
    void byDefaultTheInitialFocusIsOnTheLeftHandField() {
        FingeringDialog.Fields fields = FingeringDialog.buildFields(Optional.empty(), Optional.empty());

        assertSame(fields.leftHand(), fields.initialFocus());
    }

    @Test
    void whenTheRightHandFocusIsRequestedTheInitialFieldIsTheRightHandOne() {
        FingeringDialog.Fields fields = FingeringDialog.buildFields(
                Optional.empty(), Optional.empty(), FingeringDialog.Hand.RIGHT);

        assertSame(fields.rightHand(), fields.initialFocus());
    }
}
