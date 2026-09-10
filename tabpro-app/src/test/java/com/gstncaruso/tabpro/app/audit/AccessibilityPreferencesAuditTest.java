package com.gstncaruso.tabpro.app.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.dialogs.preferences.PreferencesPanel;
import com.gstncaruso.tabpro.ui.theme.ThemeSwitch;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * {@code @Isolated} because this writes to the same {@code java.util.prefs} node that any real
 * {@code MainFrame} in the suite uses.
 */
@Tag("integration")
@Isolated
class AccessibilityPreferencesAuditTest {

    private final com.gstncaruso.tabpro.ui.Preferences preferences = new com.gstncaruso.tabpro.ui.Preferences();

    @AfterEach
    void restoreDefaults() {
        preferences.setInterfaceFontSize(12);
        preferences.setHighContrastEnabled(false);
        preferences.setAnimationsDisabled(false);
    }

    @Test
    void startingUpAppliesThePersistedAccessibilityPreferences() throws Exception {
        preferences.setInterfaceFontSize(18);
        preferences.setHighContrastEnabled(true);
        preferences.setAnimationsDisabled(true);
        RecordingThemeSwitch themes = new RecordingThemeSwitch();

        MainFrame frame = AuditSupport.newFrame(new Editor(com.gstncaruso.tabpro.core.model.Score.blank()), themes);
        try {
            assertEquals(18, themes.lastFontSize);
            assertEquals(true, themes.lastHighContrast);
            assertEquals(false, themes.lastAnimationsEnabled);
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void acceptingPreferencesPersistsAndAppliesTheAccessibilityChoices() throws Exception {
        RecordingThemeSwitch themes = new RecordingThemeSwitch();
        MainFrame frame = AuditSupport.newFrame(
                new Editor(com.gstncaruso.tabpro.core.model.Score.blank()), themes);
        try {
            JMenuItem preferencesItem = AuditSupport.findMenuItem(frame.getJMenuBar(), "Preferencias…");
            AuditSupport.withDialog(preferencesItem::doClick, dialog -> {
                PreferencesPanel panel = AuditSupport.findComponent(dialog, PreferencesPanel.class);
                panel.apply(panel.toPreferences()
                        .withInterfaceFontSize(16)
                        .withHighContrastEnabled(true)
                        .withAnimationsDisabled(true));
                JButton accept = AuditSupport.findButton(dialog, "Aceptar");
                accept.doClick();
            });

            assertEquals(16, new com.gstncaruso.tabpro.ui.Preferences().interfaceFontSize());
            assertEquals(true, new com.gstncaruso.tabpro.ui.Preferences().highContrastEnabled());
            assertEquals(true, new com.gstncaruso.tabpro.ui.Preferences().animationsDisabled());
            assertEquals(16, themes.lastFontSize);
            assertEquals(true, themes.lastHighContrast);
            assertEquals(false, themes.lastAnimationsEnabled);
        } finally {
            AuditSupport.dispose(frame);
        }
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
