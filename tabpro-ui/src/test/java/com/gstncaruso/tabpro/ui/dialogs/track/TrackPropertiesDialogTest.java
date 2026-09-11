package com.gstncaruso.tabpro.ui.dialogs.track;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TrackPropertiesDialogTest {

    @Test
    void theTitleIsAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Track Properties", english.text("score_dialogs.TrackPropertiesDialog.title"));
    }
}
