package com.gstncaruso.tabpro.ui.i18n;

import java.util.Locale;

public enum Language {
    AUTOMATIC;

    private static final Locale SPANISH = Locale.forLanguageTag("es");

    public Locale resolve(Locale systemLocale) {
        return SPANISH;
    }
}
