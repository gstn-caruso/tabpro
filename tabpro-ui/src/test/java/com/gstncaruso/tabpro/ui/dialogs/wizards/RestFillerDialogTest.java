package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class RestFillerDialogTest {

    @Test
    void theTitleAndAcceptButtonAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Fill Bars with Rests", english.text("score_dialogs.RestFillerDialog.title"));
        assertEquals("Fill", english.text("score_dialogs.RestFillerDialog.accept"));
    }
}
