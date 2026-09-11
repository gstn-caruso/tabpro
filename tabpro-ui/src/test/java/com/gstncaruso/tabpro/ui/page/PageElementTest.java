package com.gstncaruso.tabpro.ui.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class PageElementTest {

    @Test
    void everyLabelIsAvailableInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("Title", english.text("views.PageElement.TITLE.label"));
        assertEquals("Words by", english.text("views.PageElement.WORDS.label"));
        assertEquals("Music by", english.text("views.PageElement.MUSIC.label"));
        assertEquals("Page Number", english.text("views.PageElement.PAGE_NUMBER.label"));
    }

    @Test
    void theDefaultTemplatesKeepTheirPlaceholdersInEnglish() {
        Texts english = Texts.forLocale(Locale.ENGLISH);

        assertEquals("[%title]", english.text("views.PageElement.TITLE.defaultText"));
        assertEquals("Words: [%words]", english.text("views.PageElement.WORDS.defaultText"));
        assertEquals("Page [%page] of [%pages]", english.text("views.PageElement.PAGE_NUMBER.defaultText"));
    }

    @Test
    void theSpanishDefaultsStayPinnedForTheProcessLanguage() {
        assertEquals("Página [%page] de [%pages]", PageElement.PAGE_NUMBER.defaultText());
        assertEquals("Letra: [%words]", PageElement.WORDS.defaultText());
    }
}
