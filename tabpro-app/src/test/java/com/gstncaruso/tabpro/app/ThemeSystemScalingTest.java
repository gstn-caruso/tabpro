package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatSystemProperties;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/** Isolated because LookAndFeel, UI defaults and scaling properties are JVM-wide. */
@Isolated
class ThemeSystemScalingTest {

    @Test
    void givenSystemSizedLaf_whenInstallingTheme_thenButtonKeepsSystemSize() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LookAndFeel originalLaf = UIManager.getLookAndFeel();
            Map<Object, Object> originalOverrides = uiOverrides();
            String originalScale = System.getProperty(FlatSystemProperties.UI_SCALE);
            String originalScaleEnabled = System.getProperty(FlatSystemProperties.UI_SCALE_ENABLED);
            try {
                System.clearProperty(FlatSystemProperties.UI_SCALE);
                System.setProperty(FlatSystemProperties.UI_SCALE_ENABLED, "true");
                UIManager.put("defaultFont", new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 29));
                FlatDarkLaf.setup();
                JButton reference = new JButton("Abrir");
                assertEquals(29f, reference.getFont().getSize2D(), "Controlled system-sized baseline");

                Theme.install();
                JButton themed = new JButton("Abrir");

                assertAll("Installing the theme must preserve the system-sized button",
                        () -> assertEquals(reference.getFont(), themed.getFont(), "Button font"),
                        () -> assertEquals(reference.getPreferredSize(), themed.getPreferredSize(),
                                "Button preferred size"));
            } finally {
                restoreProperty(FlatSystemProperties.UI_SCALE, originalScale);
                restoreProperty(FlatSystemProperties.UI_SCALE_ENABLED, originalScaleEnabled);
                uiOverrides().keySet().forEach(key -> UIManager.put(key, null));
                try {
                    UIManager.setLookAndFeel(originalLaf);
                } catch (javax.swing.UnsupportedLookAndFeelException exception) {
                    throw new AssertionError("Cannot restore the original LookAndFeel", exception);
                } finally {
                    originalOverrides.forEach(UIManager::put);
                }
            }
        });
    }

    @Test
    void givenSystemSizedLaf_whenIncreasingAndResettingFontSize_thenSizingStaysRelativeToSystem() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LookAndFeel originalLaf = UIManager.getLookAndFeel();
            Map<Object, Object> originalOverrides = uiOverrides();
            String originalScale = System.getProperty(FlatSystemProperties.UI_SCALE);
            String originalScaleEnabled = System.getProperty(FlatSystemProperties.UI_SCALE_ENABLED);
            try {
                System.clearProperty(FlatSystemProperties.UI_SCALE);
                System.setProperty(FlatSystemProperties.UI_SCALE_ENABLED, "true");
                UIManager.put("defaultFont", new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 29));
                FlatDarkLaf.setup();

                Theme theme = Theme.install();
                theme.useFontSize(12);
                JButton baseline = new JButton("Abrir");
                assertEquals(29f, baseline.getFont().getSize2D(), "Default adjustment keeps the system baseline");

                theme.useFontSize(16);
                JButton increased = new JButton("Abrir");
                theme.useFontSize(12);
                JButton reset = new JButton("Abrir");

                assertAll("Font adjustments stay relative to the desktop-sized baseline",
                        () -> assertEquals(baseline.getFont().getSize2D() * 16f / 12f,
                                increased.getFont().getSize2D(), 0.01f,
                                "Increasing the adjustment must enlarge, not shrink, the button font"),
                        () -> assertEquals(baseline.getFont(), reset.getFont(),
                                "Resetting to 12 restores the system font"),
                        () -> assertEquals(baseline.getPreferredSize(), reset.getPreferredSize(),
                                "Resetting to 12 restores the button size"));
            } finally {
                restoreProperty(FlatSystemProperties.UI_SCALE, originalScale);
                restoreProperty(FlatSystemProperties.UI_SCALE_ENABLED, originalScaleEnabled);
                uiOverrides().keySet().forEach(key -> UIManager.put(key, null));
                try {
                    UIManager.setLookAndFeel(originalLaf);
                } catch (javax.swing.UnsupportedLookAndFeelException exception) {
                    throw new AssertionError("Cannot restore the original LookAndFeel", exception);
                } finally {
                    originalOverrides.forEach(UIManager::put);
                }
            }
        });
    }

    private static Map<Object, Object> uiOverrides() {
        Map<Object, Object> overrides = new HashMap<>();
        // Hashtable.forEach visits developer overrides, not the merged LookAndFeel defaults.
        UIManager.getDefaults().forEach(overrides::put);
        return overrides;
    }

    private static void restoreProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }
}
