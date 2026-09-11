package com.gstncaruso.tabpro.ui.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ScoreBrowserTest {

    @Test
    void theTitleIsAvailableInEnglish() {
        assertEquals("Browse Scores", Texts.forLocale(Locale.ENGLISH).text("views.ScoreBrowser.title"));
    }
}
