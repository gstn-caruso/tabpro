package com.gstncaruso.tabpro.ui.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
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

    @Test
    void anEnglishInstanceIgnoresWhateverTheJvmDefaultLocaleHappensToBe() {
        String withoutTheNoFallbackControl =
                ResourceBundle.getBundle("com.gstncaruso.tabpro.ui.i18n.common", Locale.ENGLISH)
                        .getString("common.accept");

        assertNotEquals(withoutTheNoFallbackControl, Texts.forLocale(Locale.ENGLISH).text("common.accept"));
        assertEquals("OK", Texts.forLocale(Locale.ENGLISH).text("common.accept"));
    }

    @Test
    void placeholdersAreInterpolatedInBothLanguages() {
        assertEquals("Hola, Gastón", Texts.get("fixture.greeting", "Gastón"));
        assertEquals("Hello, Gastón", Texts.forLocale(Locale.ENGLISH).text("fixture.greeting", "Gastón"));
    }
}
