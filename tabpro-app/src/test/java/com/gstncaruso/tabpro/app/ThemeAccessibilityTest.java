package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.formdev.flatlaf.FlatSystemProperties;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * {@code @Isolated} because these tests mutate JVM-wide state (the UIManager, FlatLaf's
 * animation system property): they cannot run at the same time as another test.
 */
@Isolated
class ThemeAccessibilityTest {

    private final Theme theme = new Theme();

    private LookAndFeel originalLaf;
    private Map<Object, Object> originalOverrides;
    private String originalAnimation;

    @BeforeEach
    void rememberUiState() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            originalLaf = UIManager.getLookAndFeel();
            originalOverrides = uiOverrides();
            originalAnimation = System.getProperty(FlatSystemProperties.ANIMATION);
        });
    }

    @AfterEach
    void restoreUiState() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            uiOverrides().keySet().forEach(key -> UIManager.put(key, null));
            try {
                UIManager.setLookAndFeel(originalLaf);
            } catch (javax.swing.UnsupportedLookAndFeelException exception) {
                throw new AssertionError("Cannot restore the original LookAndFeel", exception);
            } finally {
                originalOverrides.forEach(UIManager::put);
                if (originalAnimation == null) {
                    System.clearProperty(FlatSystemProperties.ANIMATION);
                } else {
                    System.setProperty(FlatSystemProperties.ANIMATION, originalAnimation);
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

    @Test
    void changingTheFontSizeUpdatesTheDefaultFontInTheUIManager() {
        theme.useFontSize(16);

        assertEquals(16f, UIManager.getFont("defaultFont").getSize2D());
    }

    @Test
    void turningOnHighContrastPaintsItsOwnPalette() {
        theme.apply(Theme.LIGHT);

        theme.useHighContrast(true);

        assertEquals(Color.BLACK, UIManager.getColor("tabpro.background"));
    }

    @Test
    void switchingThemesWhileHighContrastIsOnKeepsItsOwnPalette() {
        theme.apply(Theme.LIGHT);
        theme.useHighContrast(true);

        theme.apply(Theme.DARK);

        assertEquals(Color.BLACK, UIManager.getColor("tabpro.background"));
    }

    @Test
    void turningHighContrastOffRestoresTheChosenTheme() {
        theme.apply(Theme.LIGHT);
        theme.useHighContrast(true);

        theme.useHighContrast(false);

        assertEquals(Theme.paletteFor(Theme.LIGHT).background(), UIManager.getColor("tabpro.background"));
    }

    @Test
    void disablingAnimationsSetsTheFlatLafSystemProperty() {
        theme.useAnimations(false);

        assertFalse(FlatSystemProperties.getBoolean(FlatSystemProperties.ANIMATION, true));
    }

    @Test
    void enablingAnimationsAgainSetsTheFlatLafSystemPropertyBackOn() {
        theme.useAnimations(false);

        theme.useAnimations(true);

        assertTrue(FlatSystemProperties.getBoolean(FlatSystemProperties.ANIMATION, false));
    }
}
