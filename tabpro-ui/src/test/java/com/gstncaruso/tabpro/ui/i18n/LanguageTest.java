package com.gstncaruso.tabpro.ui.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LanguageTest {

    private static final Locale SPANISH = Locale.forLanguageTag("es");

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"es-AR", "es-ES", "es"})
    void automaticPicksSpanishWhenTheSystemSpeaksSpanishInAnyCountry(String systemLanguageTag) {
        assertEquals(SPANISH, Language.AUTOMATIC.resolve(Locale.forLanguageTag(systemLanguageTag)));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"en-US", "pt-BR", "de", "fr", "und"})
    void automaticPicksEnglishWhenTheSystemSpeaksAnyOtherLanguage(String systemLanguageTag) {
        assertEquals(Locale.ENGLISH, Language.AUTOMATIC.resolve(Locale.forLanguageTag(systemLanguageTag)));
    }
}
