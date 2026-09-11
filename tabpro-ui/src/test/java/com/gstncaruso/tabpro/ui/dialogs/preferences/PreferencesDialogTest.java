package com.gstncaruso.tabpro.ui.dialogs.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class PreferencesDialogTest {

    @Test
    void theTitleIsAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Preferences", english.text("score_dialogs.PreferencesDialog.title"));
    }
}
