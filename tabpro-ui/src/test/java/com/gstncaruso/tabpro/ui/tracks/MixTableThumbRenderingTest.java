package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.i18n.TextsDefaultNames;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Isolated
class MixTableThumbRenderingTest {
    @ParameterizedTest
    @CsvSource({"1,0", "1,64", "1,127", "2,0", "2,64", "2,127"})
    void givenRealMixerAt1xAnd2x_whenRenderingBoundaryValues_thenThumbIsCompactBeveledAndTextFits(
            int scale, int midi) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Object originalScale = UIManager.get("laf.scaleFactor");
            try {
                UIManager.put("laf.scaleFactor", (float) scale);
                Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
                MixParameter.VOLUME.applyTo(editor, 0, midi);
                MixParameter.PAN.applyTo(editor, 0, midi);
                MixTable table = new MixTable(editor);
                table.setSize(table.getPreferredSize().width + 100 * scale, table.getPreferredSize().height);
                layout(table);
                MixTableRow row = table.rows().getFirst();
                assertAll("Real mixer at " + scale + "x, MIDI " + midi,
                        () -> assertThumb(row.volumeSlider(), midi == 0 ? 0 : midi == 64 ? 8 : 16, scale),
                        () -> assertThumb(row.panSlider(), midi == 0 ? -8 : midi == 64 ? 0 : 8, scale));
            } finally {
                UIManager.put("laf.scaleFactor", originalScale);
            }
        });
    }

    @ParameterizedTest
    @CsvSource({
        "1,VOLUME,64,YELLOW", "1,VOLUME,96,ORANGE", "1,VOLUME,127,RED",
        "1,PAN,0,GREEN", "1,PAN,64,YELLOW", "1,PAN,127,RED",
        "2,VOLUME,64,YELLOW", "2,VOLUME,96,ORANGE", "2,VOLUME,127,RED",
        "2,PAN,0,GREEN", "2,PAN,64,YELLOW", "2,PAN,127,RED"
    })
    void givenMixerLevels_whenRendered_thenThinRecessedGrooveMatchesGp5ColorsAcrossItsLength(
            int scale, MixParameter parameter, int midi, String family) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Object originalScale = UIManager.get("laf.scaleFactor");
            try {
                UIManager.put("laf.scaleFactor", (float) scale);
                Editor editor = new Editor(Score.blank(new TextsDefaultNames()));
                parameter.applyTo(editor, 0, midi);
                MixTable table = new MixTable(editor);
                table.setSize(table.getPreferredSize().width + 100 * scale, table.getPreferredSize().height);
                layout(table);
                MixTableRow row = table.rows().getFirst();
                LevelSlider slider = parameter == MixParameter.VOLUME ? row.volumeSlider() : row.panSlider();
                BufferedImage image = new BufferedImage(slider.getWidth(), slider.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();
                try {
                    slider.paint(graphics);
                } finally {
                    graphics.dispose();
                }
                // Exclude a generous 24px thumb envelope; inspect every exposed section, on either side.
                int center = (int) Math.round((image.getWidth() - 1) * midi / 127.0);
                int thumbLeft = Math.max(0, Math.min(image.getWidth() - 24 * scale, center - 12 * scale));
                var sections = new java.util.ArrayList<org.junit.jupiter.api.function.Executable>();
                for (int x = 3 * scale; x < image.getWidth() - 3 * scale; x += 6 * scale) {
                    if (x >= thumbLeft - 2 * scale && x <= thumbLeft + 26 * scale) continue;
                    int column = x;
                    sections.add(() -> assertGrooveSection(image, column, scale, family));
                }
                assertTrue(!sections.isEmpty(), "There must be an exposed groove to inspect");
                assertAll(parameter + " MIDI " + midi + " at " + scale + "x", sections);
            } finally {
                UIManager.put("laf.scaleFactor", originalScale);
            }
        });
    }

    private static void assertGrooveSection(BufferedImage image, int x, int scale, String family) {
        int top = 0;
        while (top < image.getHeight() && (image.getRGB(x, top) >>> 24) == 0) top++;
        int bottom = image.getHeight() - 1;
        while (bottom >= top && (image.getRGB(x, bottom) >>> 24) == 0) bottom--;
        int thickness = bottom - top + 1;
        java.awt.Color color = new java.awt.Color(image.getRGB(x, image.getHeight() / 2), true);
        float[] hsb = java.awt.Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float hue = hsb[0] * 360;
        boolean matches = hsb[1] >= 0.45 && hsb[2] >= 0.45 && switch (family) {
            case "GREEN" -> hue >= 75 && hue <= 160;
            case "YELLOW" -> hue >= 45 && hue <= 70;
            case "ORANGE" -> hue >= 15 && hue < 45;
            case "RED" -> hue <= 15 || hue >= 345;
            default -> false;
        };
        double upper = pixelBrightness(image.getRGB(x, Math.min(top, image.getHeight() - 1)));
        double lower = pixelBrightness(image.getRGB(x, Math.max(0, bottom)));
        assertAll("Exposed groove at x=" + x,
                () -> assertTrue(thickness >= 5 * scale && thickness <= 7 * scale,
                        "Thin GP5 groove including bevel: expected 5..7 logical px, got " + thickness / (double) scale),
                () -> assertTrue(matches && color.getAlpha() >= 240,
                        "Entire exposed groove must be " + family + ", got " + color + " hue=" + hue),
                () -> assertTrue(lower > upper + 15,
                        "Recessed groove needs darker upper edge and lighter lower edge: " + upper + " / " + lower));
    }

    private static double pixelBrightness(int rgb) {
        return (((rgb >> 16) & 255) + ((rgb >> 8) & 255) + (rgb & 255)) / 3.0;
    }

    private static void assertThumb(LevelSlider slider, int displayed, int scale) {
        assertEquals(96 * scale, slider.getWidth(), "Keep the full interaction width");
        assertEquals(28 * scale, slider.getHeight(), "Keep the full interaction height");
        BufferedImage image = new BufferedImage(slider.getWidth(), slider.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            slider.paint(graphics);
        } finally {
            graphics.dispose();
        }
        // Transparency locates the silhouette, not a text/color mask. The thumb is taller than the groove.
        int top = 0;
        while (top < image.getHeight() && occupied(image, top) == 0) top++;
        int bottom = image.getHeight() - 1;
        while (bottom >= top && occupied(image, bottom) == 0) bottom--;
        int height = bottom - top + 1;
        assertTrue(height >= 12 * scale && height <= 17 * scale,
                "Compact GP5 thumb height: expected 12..17 logical px, rendered " + height / (double) scale);
        int left = 0;
        while (left < image.getWidth() && (image.getRGB(left, top + scale) >>> 24) == 0) left++;
        int right = image.getWidth() - 1;
        while (right >= left && (image.getRGB(right, top + scale) >>> 24) == 0) right--;
        int width = right - left + 1;
        int textWidth = slider.getFontMetrics(slider.getFont()).stringWidth(String.valueOf(displayed));
        double topBrightness = edgeBrightness(image, left, right, top);
        double bottomBrightness = edgeBrightness(image, left, right, bottom);
        assertAll("Compact rectangular numbered thumb " + displayed,
                () -> assertTrue(width >= 15 * scale && width <= 24 * scale,
                        "Thumb width must stay near GP5's 17px, allowing two characters: " + width),
                () -> assertTrue(textWidth + 4 * scale <= width,
                        "Number needs horizontal breathing room inside the bevel"),
                () -> assertTrue(slider.getFontMetrics(slider.getFont()).getHeight() + 2 * scale <= height,
                        "Number must fit vertically inside the bevel"),
                () -> assertTrue(topBrightness > bottomBrightness + 15,
                        "Raised thumb needs a visibly lighter top edge than bottom edge"));
    }

    private static int occupied(BufferedImage image, int y) {
        int count = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            if ((image.getRGB(x, y) >>> 24) != 0) count++;
        }
        return count;
    }

    private static double edgeBrightness(BufferedImage image, int left, int right, int y) {
        double sum = 0;
        // Skip corners; text is inside, so it cannot be mistaken for the bevel.
        for (int x = left + 2; x <= right - 2; x++) {
            int rgb = image.getRGB(x, y);
            sum += (((rgb >> 16) & 255) + ((rgb >> 8) & 255) + (rgb & 255)) / 3.0;
        }
        return sum / (right - left - 3);
    }

    private static void layout(Container container) {
        container.doLayout();
        for (var child : container.getComponents()) {
            if (child instanceof Container nested) layout(nested);
        }
    }
}
