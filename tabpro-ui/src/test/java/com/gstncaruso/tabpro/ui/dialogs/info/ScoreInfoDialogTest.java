package com.gstncaruso.tabpro.ui.dialogs.info;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ScoreInfoDialogTest {

    @Test
    void theTitleAndTabsAreAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Score Information", english.text("score_dialogs.ScoreInfoDialog.title"));
        assertEquals("Lyrics", english.text("score_dialogs.ScoreInfoDialog.lyricsTab"));
    }
}
