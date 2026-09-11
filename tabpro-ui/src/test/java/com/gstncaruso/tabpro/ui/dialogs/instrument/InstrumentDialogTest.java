package com.gstncaruso.tabpro.ui.dialogs.instrument;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class InstrumentDialogTest {

    @Test
    void theTitleIsAvailableInEnglish() {
        assertEquals("Instrument", Texts.forLocale(Locale.ENGLISH).text("edit_dialogs.InstrumentDialog.title"));
    }
}
