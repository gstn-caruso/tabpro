package com.gstncaruso.tabpro.ui.dialogs.wave;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class WaveExportDialogTest {

    @Test
    void theTitleIsAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Export to WAVE", english.text("score_dialogs.WaveExportDialog.title"));
    }
}
