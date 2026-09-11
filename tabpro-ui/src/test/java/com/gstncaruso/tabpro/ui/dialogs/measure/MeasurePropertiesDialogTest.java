package com.gstncaruso.tabpro.ui.dialogs.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class MeasurePropertiesDialogTest {

    @Test
    void theTitleAndTimeSignatureTabAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Measure Properties", english.text("edit_dialogs.MeasurePropertiesDialog.title"));
        assertEquals("Time Signature", english.text("edit_dialogs.MeasurePropertiesDialog.timeSignature"));
    }
}
