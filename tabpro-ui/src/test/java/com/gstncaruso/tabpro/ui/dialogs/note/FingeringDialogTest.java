package com.gstncaruso.tabpro.ui.dialogs.note;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated
class FingeringDialogTest {

    @Test
    void fingerNamesAreTranslatedAlongsideTheirMusicalSymbols() {
        try {
            Texts.install(Locale.forLanguageTag("es"));
            assertEquals("1 — Índice", renderedIndexFinger());
            Texts.install(Locale.ENGLISH);
            assertEquals("1 — Index", renderedIndexFinger());
        } finally {
            Texts.install(Locale.forLanguageTag("es"));
        }
    }

    private static String renderedIndexFinger() {
        var combo = FingeringDialog.buildFields(Optional.empty(), Optional.empty()).leftHand();
        var label = (javax.swing.JLabel) combo.getRenderer().getListCellRendererComponent(
                new javax.swing.JList<>(), Finger.INDEX, 0, false, false);
        return label.getText();
    }

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
