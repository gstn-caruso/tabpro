package com.gstncaruso.tabpro.ui.dialogs.print;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class PrintDialogTest {

    @Test
    void theSharedPrintLabelIsAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Print", english.text("score_dialogs.shared.print"));
    }
}
