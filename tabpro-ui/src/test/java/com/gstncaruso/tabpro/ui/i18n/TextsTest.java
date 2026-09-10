package com.gstncaruso.tabpro.ui.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import java.util.MissingResourceException;
import org.junit.jupiter.api.Test;

class TextsTest {

    @Test
    void aKeyWithNoTranslationThrows() {
        assertThrows(MissingResourceException.class, () -> Texts.get("common.doesNotExist"));
    }

    @Test
    void theProcessLanguageDefaultsToSpanish() {
        assertEquals("Aceptar", Texts.get("common.accept"));
    }

    @Test
    void anEnglishInstanceReadsTheSameKeyInEnglish() {
        assertEquals("OK", Texts.forLocale(Locale.ENGLISH).text("common.accept"));
    }
}
