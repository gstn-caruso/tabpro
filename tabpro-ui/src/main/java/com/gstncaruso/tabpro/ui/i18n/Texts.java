package com.gstncaruso.tabpro.ui.i18n;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public final class Texts {

    public static final List<String> AREAS = List.of(
            "common", "menus", "domain", "library", "edit_dialogs",
            "score_dialogs", "views", "window", "defaults");

    private static final ResourceBundle.Control NO_FALLBACK =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

    private static final Texts PROCESS_TEXTS = new Texts(Locale.forLanguageTag("es"));

    private final Locale locale;

    private Texts(Locale locale) {
        this.locale = locale;
    }

    public static Texts forLocale(Locale locale) {
        return new Texts(locale);
    }

    public static String get(String key, Object... arguments) {
        return PROCESS_TEXTS.text(key, arguments);
    }

    public String text(String key, Object... arguments) {
        return MessageFormat.format(patternFor(key), arguments);
    }

    private String patternFor(String key) {
        return bundleFor(key).getString(key);
    }

    private ResourceBundle bundleFor(String key) {
        return ResourceBundle.getBundle(Texts.class.getPackageName() + "." + areaOf(key), locale, NO_FALLBACK);
    }

    private static String areaOf(String key) {
        return key.substring(0, key.indexOf('.'));
    }
}
