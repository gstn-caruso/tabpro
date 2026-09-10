package com.gstncaruso.tabpro.ui.dialogs.note;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.testsupport.Combos;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class DynamicsDialogTest {

    @Test
    void theTitleAndWholeChordFieldAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Dynamic", english.text("edit_dialogs.shared.dynamic"));
        assertEquals("Apply to the Chord", english.text("edit_dialogs.DynamicsDialog.wholeChord"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        DynamicsDialog.Fields fields = DynamicsDialog.buildFields(Dynamic.defaultDynamic());

        AccessibilityAssertions.assertNoViolations(fields.form());
    }

    @Test
    void theDynamicComboShowsTheMusicalSymbolInsteadOfTheRawEnum() {
        DynamicsDialog.Fields fields = DynamicsDialog.buildFields(Dynamic.defaultDynamic());

        String renderedText = Combos.renderedTextOf(fields.form(), Dynamic.class, Dynamic.MEZZO_FORTE);

        assertEquals("mf", renderedText);
    }
}
