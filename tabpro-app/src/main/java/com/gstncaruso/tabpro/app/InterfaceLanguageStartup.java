package com.gstncaruso.tabpro.app;

import com.gstncaruso.tabpro.ui.Preferences;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.util.Locale;

final class InterfaceLanguageStartup {

    private final Locale systemLocale;
    private final Preferences preferences;

    InterfaceLanguageStartup(Locale systemLocale, Preferences preferences) {
        this.systemLocale = systemLocale;
        this.preferences = preferences;
    }

    Locale interfaceLocale() {
        return preferences.interfaceLanguage().resolve(systemLocale);
    }

    void install() {
        Locale interfaceLocale = interfaceLocale();
        Locale.setDefault(interfaceLocale);
        Texts.install(interfaceLocale);
    }
}
