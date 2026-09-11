package com.gstncaruso.tabpro.ui.dialogs.help;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class HelpDialogTest {

    @Test
    void theTitleIsAvailableInEnglish() {
        assertEquals("tabpro Help", Texts.forLocale(Locale.ENGLISH).text("edit_dialogs.HelpDialog.title"));
    }
}
