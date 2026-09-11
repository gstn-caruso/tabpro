package com.gstncaruso.tabpro.ui.dialogs.paste;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.PasteOptions;
import com.gstncaruso.tabpro.ui.a11y.AccessibilityAssertions;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class PastePanelTest {

    @Test
    void theInsertAndRepetitionsFieldsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Insert", english.text("edit_dialogs.PastePanel.insert"));
        assertEquals("Repetitions", english.text("edit_dialogs.PastePanel.repetitions"));
    }

    @Test
    void everyControlHasAnAccessibleNameAndTooltip() {
        AccessibilityAssertions.assertNoViolations(new PastePanel());
    }

    @Test
    void defaultsToInsertingOnce() {
        PastePanel panel = new PastePanel();

        assertEquals(PasteOptions.insertingOnce(), panel.toPasteOptions());
    }

    @Test
    void canBeSetToReplaceSeveralTimes() {
        PastePanel panel = new PastePanel();

        panel.selectReplacing();
        panel.setRepetitions(4);

        assertEquals(new PasteOptions(false, 4), panel.toPasteOptions());
    }
}
