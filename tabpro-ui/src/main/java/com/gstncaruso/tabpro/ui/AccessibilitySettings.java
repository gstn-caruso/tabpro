package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.ui.theme.ThemeSwitch;

final class AccessibilitySettings {

    private AccessibilitySettings() {
    }

    static void applyFrom(Preferences preferences, ThemeSwitch themes) {
        themes.useFontSize(preferences.interfaceFontSize());
        themes.useHighContrast(preferences.highContrastEnabled());
        themes.useAnimations(!preferences.animationsDisabled());
    }
}
