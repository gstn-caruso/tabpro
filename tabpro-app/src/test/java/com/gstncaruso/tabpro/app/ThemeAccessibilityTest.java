package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import javax.swing.UIManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Preferencias [F12] > Accesibilidad, aplicadas de verdad sobre el UIManager. {@code @Isolated}
 * porque estos tests mutan estado global de la maquina virtual (UIManager, la propiedad de
 * animaciones de FlatLaf): no pueden correr al mismo tiempo que otro test.
 */
@Isolated
class ThemeAccessibilityTest {

    private final Theme theme = new Theme();

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
}
