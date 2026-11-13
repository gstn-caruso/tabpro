package com.gstncaruso.tabpro.ui.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
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
    void numbersAreFormattedInTheLanguageOfTheTranslation() {
        assertEquals("1,234.5", Texts.forLocale(Locale.ENGLISH).text("fixture.number", 1234.5));
        assertEquals("1.234,5", Texts.forLocale(Locale.forLanguageTag("es")).text("fixture.number", 1234.5));
    }

    @Test
    void placeholdersAreInterpolatedInBothLanguages() {
        assertEquals("Hola, Gastón", Texts.get("fixture.greeting", "Gastón"));
        assertEquals("Hello, Gastón", Texts.forLocale(Locale.ENGLISH).text("fixture.greeting", "Gastón"));
    }

    @Test
    void everyAreaHasTheSameKeysInBothLanguagesWithNoBlankValues() throws IOException {
        ResourceBundle.Control noFallback =
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

        for (String area : Texts.AREAS) {
            String baseName = "com.gstncaruso.tabpro.ui.i18n." + area;
            ResourceBundle base = ResourceBundle.getBundle(baseName, Locale.ROOT, noFallback);
            ResourceBundle spanish = ResourceBundle.getBundle(baseName, Locale.forLanguageTag("es"), noFallback);

            // ResourceBundle.keySet includes inherited English keys, hiding missing Spanish translations.
            Properties englishProperties = readProperties(area + ".properties");
            Properties spanishProperties = readProperties(area + "_es.properties");
            assertEquals(englishProperties.stringPropertyNames(), spanishProperties.stringPropertyNames(),
                    area + ": keys differ between languages");
            assertNoBlankValues(base, area);
            assertNoBlankValues(spanish, area);
            for (String key : base.keySet()) {
                // Spanish pitch labels also show the solfège name; English only shows the letter.
                if (!key.equals("domain.PitchClass.format")) {
                    assertEquals(placeholders(base.getString(key)), placeholders(spanish.getString(key)),
                            key + ": placeholders differ between languages");
                }
                new MessageFormat(base.getString(key), Locale.ENGLISH);
                new MessageFormat(spanish.getString(key), Locale.forLanguageTag("es"));
            }
        }
    }

    private static Properties readProperties(String file) throws IOException {
        Properties properties = new Properties();
        try (var reader = new InputStreamReader(Texts.class.getResourceAsStream(file), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private static java.util.Set<String> placeholders(String pattern) {
        return Pattern.compile("\\{(\\d+)[,}]").matcher(pattern).results()
                .map(match -> match.group(1)).collect(java.util.stream.Collectors.toSet());
    }

    private static void assertNoBlankValues(ResourceBundle bundle, String area) {
        for (String key : bundle.keySet()) {
            assertFalse(bundle.getString(key).isBlank(), area + "." + key + " is blank");
        }
    }
}
