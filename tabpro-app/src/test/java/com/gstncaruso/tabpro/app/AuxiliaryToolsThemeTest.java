package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import com.gstncaruso.tabpro.ui.harmony.ChordDiagramCanvas;
import com.gstncaruso.tabpro.ui.percussion.PercussionAssistant;
import com.gstncaruso.tabpro.ui.percussion.PercussionStaffPicker;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/** Isolated because installing real themes changes JVM-wide Swing defaults. */
@Isolated
class AuxiliaryToolsThemeTest {

    @Test
    void givenOpenMusicalTools_whenThemeChanges_thenSurfacesAdaptWithoutLosingSelection() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LookAndFeel originalLaf = UIManager.getLookAndFeel();
            Map<Object, Object> originalOverrides = uiOverrides();
            try {
                UIManager.put("laf.scaleFactor", 1f);
                Theme theme = Theme.install();
                Editor editor = new Editor(new Score("Tools", 120, List.of(Track.percussion("Drums"))));
                PercussionAssistant assistant = new PercussionAssistant(editor, new SilentPlayer());
                ChordDiagramCanvas chord = new ChordDiagramCanvas();
                chord.show(ChordDiagram.named("Am", List.of(0, 1, 2, 2, 0, -1)), Tuning.standard());
                JPanel tools = new JPanel();
                tools.add(assistant);
                tools.add(chord);
                JList<?> sounds = descendant(assistant, JList.class);
                sounds.setSelectedValue(38, true);
                JCheckBox electric = descendant(assistant, JCheckBox.class);
                electric.doClick();
                PercussionStaffPicker staff = descendant(assistant, PercussionStaffPicker.class);
                JLabel title = descendant(assistant, JLabel.class);
                chord.setSize(chord.getPreferredSize());
                int fingerX = chord.stringX(3);
                int fingerY = chord.fretRowY(1);
                int originalFinger = render(chord, Color.BLACK).getRGB(fingerX * 2, fingerY * 2);

                // Group both transitions so a light-mode failure does not hide dark-mode evidence.
                assertAll("Already-open tools follow the theme and retain their musical state",
                        () -> assertAppearance(theme, Theme.LIGHT, tools, assistant, sounds, staff, title,
                                electric, chord, originalFinger),
                        () -> assertAppearance(theme, Theme.DARK, tools, assistant, sounds, staff, title,
                                electric, chord, originalFinger));
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

    private static void assertAppearance(Theme theme, String name, JPanel tools, PercussionAssistant assistant,
            JList<?> sounds, PercussionStaffPicker staff, JLabel title, JCheckBox electric,
            ChordDiagramCanvas chord, int originalFinger) {
        theme.apply(name);
        // Offscreen components have no Window for FlatLaf.updateUI() to visit.
        SwingUtilities.updateComponentTreeUI(tools);
        assistant.setSize(assistant.getPreferredSize());
        layout(assistant);
        chord.setSize(chord.getPreferredSize());
        BufferedImage assistantImage = render(assistant, Color.BLACK);
        Color surface = new Color(assistantImage.getRGB(0, 0));
        BufferedImage soundsImage = render(sounds, surface);
        BufferedImage staffImage = render(staff, surface);
        BufferedImage chordImage = render(chord, surface);
        BufferedImage titleImage = render(title, surface);
        Rectangle firstSound = sounds.getCellBounds(0, 0); // Unselected text, not selection/domain colors.
        boolean light = Theme.LIGHT.equals(name);

        assertAll(name + " tools",
                () -> assertSurface(name + " assistant", assistantImage, light),
                () -> assertSurface(name + " sound list", soundsImage, light),
                () -> assertSurface(name + " percussion staff", staffImage, light),
                () -> assertSurface(name + " chord diagram", chordImage, light),
                () -> assertReadable(name + " zone title", titleImage,
                        new Rectangle(0, 0, title.getWidth(), title.getHeight()), Contrast.TEXT_MINIMUM_RATIO),
                () -> assertReadable(name + " sound name", soundsImage, firstSound, Contrast.TEXT_MINIMUM_RATIO),
                () -> assertReadable(name + " staff line", staffImage,
                        new Rectangle(30, 11, 10, 3), Contrast.GRAPHICAL_MINIMUM_RATIO),
                () -> assertReadable(name + " chord grid", chordImage,
                        new Rectangle(chord.stringX(3) - 1, chord.fretRowY(3) - 3, 3, 6),
                        Contrast.GRAPHICAL_MINIMUM_RATIO),
                () -> assertReadable(name + " fingering label", chordImage,
                        new Rectangle(chord.stringX(3) - 5, chord.fingerRowY() - 7, 10, 14),
                        Contrast.TEXT_MINIMUM_RATIO),
                () -> assertEquals(38, sounds.getSelectedValue(), name + " preserves the selected sound"),
                () -> assertTrue(electric.isSelected() && staff.preferElectric(), name + " preserves electric mode"),
                () -> assertEquals(originalFinger,
                        chordImage.getRGB(chord.stringX(3) * 2, chord.fretRowY(1) * 2),
                        name + " preserves the displayed chord finger, not an empty grid"));
    }

    private static void assertSurface(String label, BufferedImage image, boolean light) {
        Color background = new Color(image.getRGB(0, 0));
        boolean renderedLight = Contrast.ratio(background, Color.BLACK) > Contrast.ratio(background, Color.WHITE);
        assertEquals(light, renderedLight, label + " rendered background: " + background);
    }

    private static void assertReadable(String label, BufferedImage image, Rectangle region, double minimum) {
        Color background = new Color(image.getRGB(0, 0));
        double strongest = 1;
        for (int y = region.y * 2; y < (region.y + region.height) * 2; y++) {
            for (int x = region.x * 2; x < (region.x + region.width) * 2; x++) {
                strongest = Math.max(strongest, Contrast.ratio(background, new Color(image.getRGB(x, y))));
            }
        }
        assertTrue(strongest >= minimum, label + " rendered contrast: " + strongest + " (minimum " + minimum + ")");
    }

    private static BufferedImage render(JComponent component, Color background) {
        BufferedImage image = new BufferedImage(component.getWidth() * 2, component.getHeight() * 2,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(background);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.scale(2, 2); // Fully covered glyph/stroke pixels, as in ScoreThemeTest.
            component.paint(graphics);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container nested) {
                layout(nested);
            }
        }
    }

    private static <T> T descendant(Container root, Class<T> type) {
        return descendants(root).filter(type::isInstance).map(type::cast).findFirst().orElseThrow();
    }

    private static Stream<Component> descendants(Container root) {
        return Arrays.stream(root.getComponents()).flatMap(child -> child instanceof Container nested
                ? Stream.concat(Stream.of(child), descendants(nested)) : Stream.of(child));
    }

    private static Map<Object, Object> uiOverrides() {
        Map<Object, Object> overrides = new HashMap<>();
        // Hashtable.forEach visits developer overrides, not merged LookAndFeel defaults.
        UIManager.getDefaults().forEach(overrides::put);
        return overrides;
    }

    private static final class SilentPlayer implements Player {
        @Override public void play(Timeline timeline, PlaybackListener listener) { }
        @Override public void playNote(Pitch pitch, int program) { }
        @Override public void stop() { }
        @Override public boolean isPlaying() { return false; }
    }
}
