package com.gstncaruso.tabpro.app.audit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatSystemProperties;
import com.gstncaruso.tabpro.app.Theme;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.dialogs.preferences.PreferencesPanel;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import com.gstncaruso.tabpro.ui.theme.ThemeSwitch;
import com.gstncaruso.tabpro.ui.status.StatusBar;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
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
    void givenDefaultAccessibility_whenStartingUi_thenSystemSizingSurvives() throws Exception {
        LookAndFeel originalLaf = UIManager.getLookAndFeel();
        Map<Object, Object> originalOverrides = new HashMap<>();
        UIManager.getDefaults().forEach(originalOverrides::put);
        String originalScale = System.getProperty(FlatSystemProperties.UI_SCALE);
        String originalScaleEnabled = System.getProperty(FlatSystemProperties.UI_SCALE_ENABLED);
        String originalAnimation = System.getProperty(FlatSystemProperties.ANIMATION);
        JMenu[] reference = new JMenu[1];
        Theme[] themes = new Theme[1];
        MainFrame frame = null;
        try {
            preferences.setInterfaceFontSize(12);
            preferences.setHighContrastEnabled(false);
            preferences.setAnimationsDisabled(false);
            SwingUtilities.invokeAndWait(() -> {
                System.clearProperty(FlatSystemProperties.UI_SCALE);
                System.setProperty(FlatSystemProperties.UI_SCALE_ENABLED, "true");
                UIManager.put("defaultFont", new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 29));
                FlatDarkLaf.setup();
                reference[0] = new JMenu("Archivo");
                assertEquals(29f, reference[0].getFont().getSize2D(), "Controlled system-sized baseline");
                themes[0] = Theme.install();
            });

            frame = AuditSupport.newFrame(
                    new Editor(com.gstncaruso.tabpro.core.model.Score.blank(new TextsDefaultNames())), themes[0]);
            MainFrame started = frame;
            SwingUtilities.invokeAndWait(() -> assertEquals(
                    reference[0].getFont().getSize2D(),
                    started.getJMenuBar().getMenu(0).getFont().getSize2D(),
                    "Default accessibility must preserve the system-sized menu after startup"));
        } finally {
            try {
                if (frame != null) {
                    AuditSupport.dispose(frame);
                }
            } finally {
                SwingUtilities.invokeAndWait(() -> {
                    restoreProperty(FlatSystemProperties.UI_SCALE, originalScale);
                    restoreProperty(FlatSystemProperties.UI_SCALE_ENABLED, originalScaleEnabled);
                    restoreProperty(FlatSystemProperties.ANIMATION, originalAnimation);
                    Map<Object, Object> overrides = new HashMap<>();
                    UIManager.getDefaults().forEach(overrides::put);
                    overrides.keySet().forEach(key -> UIManager.put(key, null));
                    try {
                        UIManager.setLookAndFeel(originalLaf);
                    } catch (javax.swing.UnsupportedLookAndFeelException exception) {
                        throw new AssertionError("Cannot restore the original LookAndFeel", exception);
                    } finally {
                        originalOverrides.forEach(UIManager::put);
                    }
                });
            }
        }
    }

    @Test
    void givenDoubleUiScale_whenOpeningMainWindow_thenWindowAndTransportElementsKeepTheirLogicalSize()
            throws Exception {
        LookAndFeel originalLaf = UIManager.getLookAndFeel();
        Map<Object, Object> originalOverrides = new HashMap<>();
        UIManager.getDefaults().forEach(originalOverrides::put);
        String originalScale = System.getProperty(FlatSystemProperties.UI_SCALE);
        String originalScaleEnabled = System.getProperty(FlatSystemProperties.UI_SCALE_ENABLED);
        String originalAnimation = System.getProperty(FlatSystemProperties.ANIMATION);
        int originalFontSize = preferences.interfaceFontSize();
        boolean originalHighContrast = preferences.highContrastEnabled();
        boolean originalAnimationsDisabled = preferences.animationsDisabled();
        MainFrame[] frame = new MainFrame[1];
        try {
            preferences.setInterfaceFontSize(12);
            preferences.setHighContrastEnabled(false);
            preferences.setAnimationsDisabled(false);
            SwingUtilities.invokeAndWait(() -> {
                System.clearProperty(FlatSystemProperties.UI_SCALE);
                System.setProperty(FlatSystemProperties.UI_SCALE_ENABLED, "true");
                UIManager.put("defaultFont", new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 29));
                FlatDarkLaf.setup();
                Theme themes = Theme.install();
                assertEquals(2f, UIScale.getUserScaleFactor(), "Controlled native UI scale");
                assertEquals(29f, new JLabel().getFont().getSize2D(), "Controlled system font");
                ScoreFiles noFiles = new ScoreFiles() {
                    @Override
                    public Score load(Path path) {
                        throw new AssertionError("Opening a blank window must not load a file");
                    }

                    @Override
                    public void save(Score score, Path path) {
                        throw new AssertionError("Opening a blank window must not save a file");
                    }
                };
                // Do not pack: the constructor chooses the initial application window size.
                frame[0] = new MainFrame(AuditSupport.blankEditor(), noFiles,
                        new AuditSupport.RecordingPlayer(), themes,
                        com.gstncaruso.tabpro.ui.actions.Ports.Devices.NONE,
                        com.gstncaruso.tabpro.core.files.ScoreExchange.NONE,
                        com.gstncaruso.tabpro.ui.actions.Ports.Microphone.NONE,
                        new com.gstncaruso.tabpro.ui.print.SystemPrinting());
                frame[0].setVisible(true);
            });
            SwingUtilities.invokeAndWait(() -> {
                MainFrame opened = frame[0];
                Dimension screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getMaximumWindowBounds().getSize();
                JSpinner tempo = AuditSupport.findComponent(opened, JSpinner.class);
                assertNotNull(tempo, "The transport must expose its tempo spinner");
                JSpinner.DefaultEditor tempoEditor = (JSpinner.DefaultEditor) tempo.getEditor();
                var tempoText = tempoEditor.getTextField();
                StatusBar status = AuditSupport.findComponent(opened, StatusBar.class);
                assertNotNull(status, "The main window must show its status bar");
                List<JLabel> labels = AuditSupport.findComponents(status, JLabel.class);
                assertTrue(!labels.isEmpty(), "The status bar must contain readable labels");
                assertAll("Initial main-window layout at native 2x scale",
                        () -> assertEquals(new Dimension(Math.min(1440 * 2, screen.width - 60 * 2),
                                        Math.min(950 * 2, screen.height - 60 * 2)), opened.getSize(),
                                "Initial window must scale its logical size and screen margin"),
                        () -> assertEquals(new Dimension(140, 48), tempo.getPreferredSize(),
                                "Tempo preferred size must scale with the UI"),
                        () -> assertEquals(new Dimension(140, 48), tempo.getMaximumSize(),
                                "Tempo maximum size must not constrain the scaled editor"),
                        () -> assertTrue(tempoText.getHeight() - tempoText.getInsets().top
                                        - tempoText.getInsets().bottom
                                        >= tempoText.getFontMetrics(tempoText.getFont()).getHeight(),
                                "The visible tempo editor must fit its system-sized font vertically"),
                        () -> assertAll("Status text remains legible at 2x", labels.stream()
                                .map(label -> (org.junit.jupiter.api.function.Executable) () -> assertEquals(
                                        22f, label.getFont().getSize2D(),
                                        "Status label must scale its logical 11-point font"))));
            });
        } finally {
            try {
                if (frame[0] != null) {
                    AuditSupport.dispose(frame[0]);
                }
            } finally {
                preferences.setInterfaceFontSize(originalFontSize);
                preferences.setHighContrastEnabled(originalHighContrast);
                preferences.setAnimationsDisabled(originalAnimationsDisabled);
                SwingUtilities.invokeAndWait(() -> {
                    restoreProperty(FlatSystemProperties.UI_SCALE, originalScale);
                    restoreProperty(FlatSystemProperties.UI_SCALE_ENABLED, originalScaleEnabled);
                    restoreProperty(FlatSystemProperties.ANIMATION, originalAnimation);
                    Map<Object, Object> overrides = new HashMap<>();
                    UIManager.getDefaults().forEach(overrides::put);
                    overrides.keySet().forEach(key -> UIManager.put(key, null));
                    try {
                        UIManager.setLookAndFeel(originalLaf);
                    } catch (javax.swing.UnsupportedLookAndFeelException exception) {
                        throw new AssertionError("Cannot restore the original LookAndFeel", exception);
                    } finally {
                        originalOverrides.forEach(UIManager::put);
                    }
                });
            }
        }
    }

    private static void restoreProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    @Test
    void startingUpAppliesThePersistedAccessibilityPreferences() throws Exception {
        preferences.setInterfaceFontSize(18);
        preferences.setHighContrastEnabled(true);
        preferences.setAnimationsDisabled(true);
        RecordingThemeSwitch themes = new RecordingThemeSwitch();

        MainFrame frame = AuditSupport.newFrame(new Editor(com.gstncaruso.tabpro.core.model.Score.blank(new TextsDefaultNames())), themes);
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
                new Editor(com.gstncaruso.tabpro.core.model.Score.blank(new TextsDefaultNames())), themes);
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
