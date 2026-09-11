package com.gstncaruso.tabpro.ui.dialogs.ascii;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class AsciiImportDialogTest {

    @Test
    void theTitleAndAcceptButtonAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("ASCII Import", english.text("score_dialogs.AsciiImportDialog.title"));
        assertEquals("Import", english.text("score_dialogs.AsciiImportDialog.accept"));
    }
}
