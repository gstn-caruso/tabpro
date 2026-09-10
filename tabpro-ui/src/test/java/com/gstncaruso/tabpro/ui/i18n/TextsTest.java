package com.gstncaruso.tabpro.ui.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void placeholdersAreInterpolatedInBothLanguages() {
        assertEquals("Hola, Gastón", Texts.get("fixture.greeting", "Gastón"));
        assertEquals("Hello, Gastón", Texts.forLocale(Locale.ENGLISH).text("fixture.greeting", "Gastón"));
    }

    @Test
    void everyAreaHasTheSameKeysInBothLanguagesWithNoBlankValues() {
        ResourceBundle.Control noFallback =
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

        for (String area : Texts.AREAS) {
            String baseName = "com.gstncaruso.tabpro.ui.i18n." + area;
            ResourceBundle base = ResourceBundle.getBundle(baseName, Locale.ROOT, noFallback);
            ResourceBundle spanish = ResourceBundle.getBundle(baseName, Locale.forLanguageTag("es"), noFallback);

            assertEquals(base.keySet(), spanish.keySet(), area + ": keys differ between languages");
            assertNoBlankValues(base, area);
            assertNoBlankValues(spanish, area);
        }
    }

    private static void assertNoBlankValues(ResourceBundle bundle, String area) {
        for (String key : bundle.keySet()) {
            assertFalse(bundle.getString(key).isBlank(), area + "." + key + " is blank");
        }
    }
}
