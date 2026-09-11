package com.gstncaruso.tabpro.ui.dialogs.paste;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class PasteDialogTest {

    @Test
    void theTitleIsAvailableInEnglish() {
        assertEquals("Paste", Texts.forLocale(Locale.ENGLISH).text("edit_dialogs.PasteDialog.title"));
    }
}
