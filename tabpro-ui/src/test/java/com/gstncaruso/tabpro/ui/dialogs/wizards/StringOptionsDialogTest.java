package com.gstncaruso.tabpro.ui.dialogs.wizards;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class StringOptionsDialogTest {

    @Test
    void everyOptionHasItsOwnTitle() {
        assertEquals("Opciones de let ring", StringOptionsDialog.titleFor(StringOptionsDialog.Option.LET_RING));
        assertEquals("Opciones de palm mute", StringOptionsDialog.titleFor(StringOptionsDialog.Option.PALM_MUTE));
        assertEquals("Opciones de dinámica", StringOptionsDialog.titleFor(StringOptionsDialog.Option.DYNAMIC));
    }

    @Test
    void everyOptionTitleIsAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Let Ring Options", english.text("score_dialogs.StringOptionsDialog.letRingTitle"));
        assertEquals("Palm Mute Options", english.text("score_dialogs.StringOptionsDialog.palmMuteTitle"));
        assertEquals("Dynamic Options", english.text("score_dialogs.StringOptionsDialog.dynamicTitle"));
    }
}
