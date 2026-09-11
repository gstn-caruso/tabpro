package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ToggleChoiceTest {

    @Test
    void everyChoiceLabelIsAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("No Change", english.text("score_dialogs.shared.noChange"));
        assertEquals("On", english.text("score_dialogs.ToggleChoice.on"));
        assertEquals("Off", english.text("score_dialogs.ToggleChoice.off"));
    }
}
