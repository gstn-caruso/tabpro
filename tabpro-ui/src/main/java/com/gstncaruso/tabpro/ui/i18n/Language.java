package com.gstncaruso.tabpro.ui.i18n;

import java.util.Locale;

public enum Language {
    AUTOMATIC,
    SPANISH,
    ENGLISH;

    private static final Locale SPANISH_LOCALE = Locale.forLanguageTag("es");

    public Locale resolve(Locale systemLocale) {
        return switch (this) {
            case AUTOMATIC -> speaksSpanish(systemLocale) ? SPANISH_LOCALE : Locale.ENGLISH;
            case SPANISH -> SPANISH_LOCALE;
            case ENGLISH -> Locale.ENGLISH;
        };
    }

    private static boolean speaksSpanish(Locale locale) {
        return SPANISH_LOCALE.getLanguage().equals(locale.getLanguage());
    }
}
