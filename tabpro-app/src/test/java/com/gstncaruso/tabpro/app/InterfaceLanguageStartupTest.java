package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.Preferences;
import com.gstncaruso.tabpro.ui.i18n.Language;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class InterfaceLanguageStartupTest {

    private final java.util.prefs.Preferences node = java.util.prefs.Preferences.userRoot()
            .node("tabpro-test/" + getClass().getSimpleName() + "/" + UUID.randomUUID());
    private final Preferences preferences = new Preferences(node);

    @AfterEach
    void cleanUp() throws Exception {
        node.removeNode();
    }

    @Test
    void aStoredEnglishChoiceWinsOverASpanishSystem() {
        preferences.setInterfaceLanguage(Language.ENGLISH);

        InterfaceLanguageStartup startup = new InterfaceLanguageStartup(Locale.forLanguageTag("es-AR"), preferences);

        assertEquals(Locale.ENGLISH, startup.interfaceLocale());
    }
}
