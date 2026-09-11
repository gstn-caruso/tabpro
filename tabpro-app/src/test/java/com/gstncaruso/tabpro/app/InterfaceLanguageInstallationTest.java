package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.ui.Preferences;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated
class InterfaceLanguageInstallationTest {

    private final java.util.prefs.Preferences node = java.util.prefs.Preferences.userRoot()
            .node("tabpro-test/" + getClass().getSimpleName() + "/" + UUID.randomUUID());

    @AfterEach
    void cleanUp() throws Exception {
        node.removeNode();
    }

    @Test
    void installingMakesTheResolvedLocaleTheJvmDefaultAndTheLanguageOfEveryText() {
        Locale previousDefault = Locale.getDefault();
        InterfaceLanguageStartup startup =
                new InterfaceLanguageStartup(Locale.forLanguageTag("pt-BR"), new Preferences(node));
        try {
            startup.install();

            assertEquals(Locale.ENGLISH, Locale.getDefault());
            assertEquals("OK", Texts.get("common.accept"));
        } finally {
            Locale.setDefault(previousDefault);
            Texts.install(Locale.forLanguageTag("es"));
        }
    }
}
