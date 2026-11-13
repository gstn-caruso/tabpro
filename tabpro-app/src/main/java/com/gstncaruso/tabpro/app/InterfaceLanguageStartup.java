package com.gstncaruso.tabpro.app;

import com.gstncaruso.tabpro.ui.Preferences;
import com.gstncaruso.tabpro.ui.i18n.SwingTexts;
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
        // Native print dialogs also load resources through the general default locale.
        Locale formatLocale = Locale.getDefault(Locale.Category.FORMAT);
        Locale.setDefault(interfaceLocale);
        Locale.setDefault(Locale.Category.FORMAT, formatLocale);
        Texts.install(interfaceLocale);
        SwingTexts.install(interfaceLocale);
    }
}
