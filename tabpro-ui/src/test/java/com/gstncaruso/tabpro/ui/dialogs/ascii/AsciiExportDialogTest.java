package com.gstncaruso.tabpro.ui.dialogs.ascii;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class AsciiExportDialogTest {

    @Test
    void theTitleAndFileFilterAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("ASCII Export", english.text("score_dialogs.AsciiExportDialog.title"));
        assertEquals("ASCII Tablature (*.tab)", english.text("score_dialogs.AsciiExportDialog.fileFilter"));
    }
}
