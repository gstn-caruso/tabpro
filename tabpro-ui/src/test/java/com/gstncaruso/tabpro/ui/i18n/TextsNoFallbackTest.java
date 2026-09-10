package com.gstncaruso.tabpro.ui.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated
class TextsNoFallbackTest {

    @Test
    void anEnglishInstanceIgnoresWhateverTheJvmDefaultLocaleHappensToBe() {
        Locale previousDefault = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("es"));
        try {
            assertEquals("OK", Texts.forLocale(Locale.ENGLISH).text("common.accept"));
        } finally {
            Locale.setDefault(previousDefault);
        }
    }
}
