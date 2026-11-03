package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.ui.score.ScoreColors;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import com.gstncaruso.tabpro.ui.tracks.LevelSlider;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/** Isolated because installing the real themes changes JVM-wide Swing defaults. */
@Isolated
class MixerThemeTest {

    @Test
    void givenMixerLevel_whenRenderedInLightAndDark_thenBadgeAdaptsAndValueRemainsReadable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LookAndFeel originalLaf = UIManager.getLookAndFeel();
            Map<Object, Object> originalOverrides = uiOverrides();
            try {
                UIManager.put("laf.scaleFactor", 1f);
                Theme theme = Theme.install();
                LevelSlider slider = new LevelSlider(0, 127, 64, ScoreColors.VOLUME_LEVEL, ScoreColors.MUTED_INK);
                slider.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                slider.setSize(128, 32);

                theme.apply(Theme.LIGHT);
                RenderedBadge light = renderBadge(slider);
                assertEquals(64, slider.getValue(), "Light theme preserves the mixer level");
                theme.apply(Theme.DARK);
                RenderedBadge dark = renderBadge(slider);

                assertAll("The rendered mixer number follows the theme without losing readability or value",
                        () -> assertNotEquals(light.surface(), dark.surface(),
                                "The badge surface must adapt to light and dark, not remain fixed silver"),
                        () -> assertTrue(light.textContrast() >= Contrast.TEXT_MINIMUM_RATIO,
                                "Light badge text contrast: " + light.textContrast()),
                        () -> assertTrue(dark.textContrast() >= Contrast.TEXT_MINIMUM_RATIO,
                                "Dark badge text contrast: " + dark.textContrast()),
                        () -> assertEquals(64, slider.getValue(), "Dark theme preserves the mixer level"),
                        () -> assertEquals("64", slider.getToolTipText(), "The level remains available as text"),
                        () -> assertEquals(64, slider.getAccessibleContext().getAccessibleValue()
                                .getCurrentAccessibleValue().intValue(), "The accessible level remains unchanged"));
            } finally {
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

    private static RenderedBadge renderBadge(LevelSlider slider) {
        BufferedImage image = new BufferedImage(slider.getWidth(), slider.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            slider.paint(graphics);
        } finally {
            graphics.dispose();
        }
        // The midpoint number's interior: excludes the badge frame and the colored level track.
        int[] pixels = image.getRGB(slider.getWidth() / 2 - 7, slider.getHeight() / 2 - 5,
                14, 10, null, 0, 14);
        Map<Integer, Long> counts = Arrays.stream(pixels).boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Color surface = new Color(counts.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey());
        double textContrast = Arrays.stream(pixels)
                .mapToDouble(pixel -> Contrast.ratio(surface, new Color(pixel)))
                .max().orElseThrow();
        return new RenderedBadge(surface, textContrast);
    }

    private static Map<Object, Object> uiOverrides() {
        Map<Object, Object> overrides = new HashMap<>();
        // Hashtable.forEach visits developer overrides, not the merged LookAndFeel defaults.
        UIManager.getDefaults().forEach(overrides::put);
        return overrides;
    }

    private record RenderedBadge(Color surface, double textContrast) {
    }
}
