package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.ui.theme.ThemeSwitch;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock(RealPreferencesTests.SHARED_PREFERENCES_FILESYSTEM_NODE_LOCK)
class AccessibilitySettingsTest {

    private final java.util.prefs.Preferences node =
            java.util.prefs.Preferences.userRoot().node("com/gstncaruso/tabpro/test/" + UUID.randomUUID());
    private final Preferences preferences = new Preferences(node);
    private final RecordingThemeSwitch themes = new RecordingThemeSwitch();

    @AfterEach
    void cleanUp() throws Exception {
        node.removeNode();
    }

    @Test
    void appliesTheStoredFontSize() {
        preferences.setInterfaceFontSize(18);

        AccessibilitySettings.applyFrom(preferences, themes);

        assertEquals(18, themes.lastFontSize);
    }

    @Test
    void appliesTheStoredHighContrastChoice() {
        preferences.setHighContrastEnabled(true);

        AccessibilitySettings.applyFrom(preferences, themes);

        assertTrue(themes.lastHighContrast);
    }

    @Test
    void turnsAnimationsOffWhenTheyAreDisabled() {
        preferences.setAnimationsDisabled(true);

        AccessibilitySettings.applyFrom(preferences, themes);

        assertFalse(themes.lastAnimationsEnabled);
    }

    private static final class RecordingThemeSwitch implements ThemeSwitch {
        private int lastFontSize;
        private boolean lastHighContrast;
        private boolean lastAnimationsEnabled = true;

        @Override
        public List<String> names() {
            return List.of();
        }

        @Override
        public String current() {
            return "";
        }

        @Override
        public void apply(String name) {
        }

        @Override
        public void useFontSize(int points) {
            lastFontSize = points;
        }

        @Override
        public void useHighContrast(boolean enabled) {
            lastHighContrast = enabled;
        }

        @Override
        public void useAnimations(boolean enabled) {
            lastAnimationsEnabled = enabled;
        }
    }
}
