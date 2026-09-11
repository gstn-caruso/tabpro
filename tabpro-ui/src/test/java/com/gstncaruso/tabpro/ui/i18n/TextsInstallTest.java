package com.gstncaruso.tabpro.ui.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated
class TextsInstallTest {

    @Test
    void theInstalledLanguageIsTheOneEveryProcessTextIsReadIn() {
        Texts.install(Locale.ENGLISH);
        try {
            assertEquals("OK", Texts.get("common.accept"));
        } finally {
            Texts.install(Locale.forLanguageTag("es"));
        }
    }
}
