package com.gstncaruso.tabpro.ui.icons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class GlyphIconTest {

    private static final Color THEME_COLOR = new Color(10, 20, 30);
    private static final String NOTEHEAD_BLACK = "";

    @Test
    void necesitaAlMenosUnRenglonDeGlifos() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new GlyphIcon(18));

        assertTrue(error.getMessage().contains("glifo"), error.getMessage());
    }

    @Test
    void reportaClaroSiBravuraNoTieneElCodepointPedido() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new GlyphIcon(18, "A"));

        assertTrue(error.getMessage().contains("U+0041"), error.getMessage());
    }

    @Test
    void unGlifoSoloSePintaDelForegroundYMideElTamanoPedido() {
        GlyphIcon icon = new GlyphIcon(18, NOTEHEAD_BLACK);

        assertEquals(18, icon.getIconWidth());
        assertEquals(18, icon.getIconHeight());
        assertTrue(hasAPixelOfTheThemeColor(paint(icon)));
    }

    private static BufferedImage paint(GlyphIcon icon) {
        JPanel probe = new JPanel();
        probe.setForeground(THEME_COLOR);
        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas = image.createGraphics();
        icon.paintIcon(probe, canvas, 0, 0);
        canvas.dispose();
        return image;
    }

    private static boolean hasAPixelOfTheThemeColor(BufferedImage image) {
        int themeRgb = THEME_COLOR.getRGB() & 0xFFFFFF;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int pixel = image.getRGB(x, y);
                boolean visible = (pixel >>> 24) != 0;
                if (visible && (pixel & 0xFFFFFF) == themeRgb) {
                    return true;
                }
            }
        }
        return false;
    }
}
