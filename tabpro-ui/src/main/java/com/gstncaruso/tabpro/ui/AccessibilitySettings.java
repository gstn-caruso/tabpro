package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.ui.theme.ThemeSwitch;

/** Preferencias [F12] > Accesibilidad: como se leen y se aplican, al arrancar y al aceptar. */
final class AccessibilitySettings {

    private AccessibilitySettings() {
    }

    static void applyFrom(Preferences preferences, ThemeSwitch themes) {
        themes.useFontSize(preferences.interfaceFontSize());
        themes.useHighContrast(preferences.highContrastEnabled());
        themes.useAnimations(!preferences.animationsDisabled());
    }
}
