package com.gstncaruso.tabpro.ui.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class RelativeTempoDialogTest {

    @Test
    void theFieldsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Relative Tempo", english.text("views.RelativeTempoDialog.title"));
        assertEquals("Playback Speed", english.text("views.RelativeTempoDialog.playbackSpeed"));
    }
}
