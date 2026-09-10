package com.gstncaruso.tabpro.ui.icons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class GlyphIconTest {

    private static final Color THEME_COLOR = new Color(10, 20, 30);
    private static final String NOTEHEAD_BLACK = "";

    @Test
    void needsAtLeastOneRowOfGlyphs() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new GlyphIcon(18));

        assertTrue(error.getMessage().contains("glifo"), error.getMessage());
    }

    @Test
    void clearlyReportsWhenBravuraLacksTheRequestedCodepoint() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new GlyphIcon(18, "A"));

        assertTrue(error.getMessage().contains("U+0041"), error.getMessage());
    }

    @Test
    void aSingleGlyphPaintsOnlyInTheForegroundAndMeasuresTheRequestedSize() {
        GlyphIcon icon = new GlyphIcon(18, NOTEHEAD_BLACK);

        assertEquals(18, icon.getIconWidth());
        assertEquals(18, icon.getIconHeight());
        assertTrue(hasAPixelOfTheThemeColor(paint(icon)));
    }

    @Test
    void aSmallRowNextToALargeOneStillPaintsBoth() {
        GlyphIcon icon = new GlyphIcon(18, "", NOTEHEAD_BLACK);

        BufferedImage image = paint(icon);

        assertTrue(hasAPixelOfTheThemeColor(image), "no pinto nada");
        assertTrue(hasAPixelOfTheThemeColorAbove(image, 9), "no pinto el renglon de arriba");
        assertTrue(hasAPixelOfTheThemeColorBelow(image, 9), "no pinto el renglon de abajo");
    }

    private static boolean hasAPixelOfTheThemeColorAbove(BufferedImage image, int y) {
        return hasAPixelOfTheThemeColorInRows(image, 0, y);
    }

    private static boolean hasAPixelOfTheThemeColorBelow(BufferedImage image, int y) {
        return hasAPixelOfTheThemeColorInRows(image, y, image.getHeight());
    }

    private static boolean hasAPixelOfTheThemeColorInRows(BufferedImage image, int fromY, int toY) {
        int themeRgb = THEME_COLOR.getRGB() & 0xFFFFFF;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = fromY; y < toY; y++) {
                int pixel = image.getRGB(x, y);
                boolean visible = (pixel >>> 24) != 0;
                if (visible && (pixel & 0xFFFFFF) == themeRgb) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void overlayingTheSameGlyphTwicePaintsTheSameAsOnce() {
        Icon once = new GlyphIcon(18, NOTEHEAD_BLACK);
        Icon overlaid = GlyphIcon.overlaid(18, NOTEHEAD_BLACK, NOTEHEAD_BLACK);

        assertEquals(pixelsOf(paint(once)), pixelsOf(paint(overlaid)));
    }

    private static String pixelsOf(BufferedImage image) {
        StringBuilder pixels = new StringBuilder();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                pixels.append(Integer.toHexString(image.getRGB(x, y)));
            }
        }
        return pixels.toString();
    }

    private static BufferedImage paint(Icon icon) {
        JPanel probe = new JPanel();
        probe.setForeground(THEME_COLOR);
        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas = image.createGraphics();
        icon.paintIcon(probe, canvas, 0, 0);
        canvas.dispose();
        return image;
    }

    @Test
    void noPixelEscapesTheSquareEvenWithSeveralRows() {
        GlyphIcon icon = new GlyphIcon(18, "", NOTEHEAD_BLACK);
        int padding = 6;
        JPanel probe = new JPanel();
        probe.setForeground(THEME_COLOR);
        int canvasSize = icon.getIconWidth() + padding * 2;
        BufferedImage image = new BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas = image.createGraphics();
        icon.paintIcon(probe, canvas, padding, padding);
        canvas.dispose();

        assertTrue(everyThemeColorPixelIsInside(image, padding, icon.getIconWidth(), icon.getIconHeight()));
    }

    private static boolean everyThemeColorPixelIsInside(BufferedImage image, int padding, int width, int height) {
        int themeRgb = THEME_COLOR.getRGB() & 0xFFFFFF;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int pixel = image.getRGB(x, y);
                boolean visible = (pixel >>> 24) != 0;
                boolean themeColored = visible && (pixel & 0xFFFFFF) == themeRgb;
                boolean outsideTheIcon = x < padding || y < padding || x >= padding + width || y >= padding + height;
                if (themeColored && outsideTheIcon) {
                    return false;
                }
            }
        }
        return true;
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
