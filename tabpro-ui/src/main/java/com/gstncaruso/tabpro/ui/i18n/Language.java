package com.gstncaruso.tabpro.ui.i18n;

import java.util.Locale;

public enum Language {
    AUTOMATIC;

    private static final Locale SPANISH = Locale.forLanguageTag("es");

    public Locale resolve(Locale systemLocale) {
        return speaksSpanish(systemLocale) ? SPANISH : Locale.ENGLISH;
    }

    private static boolean speaksSpanish(Locale locale) {
        return SPANISH.getLanguage().equals(locale.getLanguage());
    }
}
