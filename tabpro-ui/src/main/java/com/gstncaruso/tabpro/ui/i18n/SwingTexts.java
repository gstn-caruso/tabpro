package com.gstncaruso.tabpro.ui.i18n;

import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.UIManager;

/** Installs the interface language for standard dialogs as well as application widgets. */
public final class SwingTexts {

    private SwingTexts() {
    }

    public static void install(Locale locale) {
        JComponent.setDefaultLocale(locale);
        ResourceBundle bundle = ResourceBundle.getBundle(
                Texts.class.getPackageName() + ".swing", locale,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        // Developer defaults survive look-and-feel changes (including system theme changes).
        for (String key : bundle.keySet()) {
            UIManager.put(key.substring("swing.".length()), bundle.getString(key));
        }
    }
}
