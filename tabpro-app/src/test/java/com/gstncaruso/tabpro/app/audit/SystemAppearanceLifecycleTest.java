package com.gstncaruso.tabpro.app.audit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.formdev.flatlaf.FlatSystemProperties;
import com.gstncaruso.tabpro.app.SystemAppearance;
import com.gstncaruso.tabpro.app.Theme;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.dialogs.preferences.PreferencesPanel;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import com.gstncaruso.tabpro.ui.score.ScoreLayout;
import com.gstncaruso.tabpro.ui.score.ViewMode;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import com.gstncaruso.tabpro.ui.tracks.MixTable;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/** Real window and rendered notation; only the desktop preference source is fake. */
@Tag("integration")
@Isolated
class SystemAppearanceLifecycleTest {

    @Test
    void givenLightSystem_whenAppearanceChanges_thenOpenUiAndScoreFollowSystem() throws Exception {
        LookAndFeel originalLaf = UIManager.getLookAndFeel();
        Map<Object, Object> originalOverrides = uiOverrides();
        String originalAnimation = System.getProperty(FlatSystemProperties.ANIMATION);
        FakeSystemAppearance system = new FakeSystemAppearance();
        Theme theme = new Theme();
        AutoCloseable[] following = new AutoCloseable[1];
        MainFrame frame = null;
        try {
            SwingUtilities.invokeAndWait(() -> {
                UIManager.put("laf.scaleFactor", 1f);
                following[0] = theme.followSystem(system);
            });
            Note note = new Note(4, 3); // F in a staff space, not on a line.
            Measure measure = new Measure(new TimeSignature(1, 4),
                    List.of(Beat.of(Duration.quarter(), note)));
            Score score = new Score("System appearance", 120,
                    List.of(Track.standardGuitar("Guitarra").withMeasures(List.of(measure))));
            frame = AuditSupport.newFrame(new Editor(score), theme);
            MainFrame openWindow = frame;
            SwingUtilities.invokeAndWait(() -> {
                ScoreCanvas canvas = AuditSupport.findComponent(openWindow.getContentPane(), ScoreCanvas.class);
                assertNotNull(canvas);
                canvas.setViewMode(ViewMode.SCREEN_HORIZONTAL);
            });

            assertAppearance(openWindow, score, true);
            // Desktop events arrive off the EDT. No theme action or test-only UI refresh follows them.
            system.changeTo(Theme.DARK);
            assertAppearance(openWindow, score, false);
            system.changeTo(Theme.LIGHT);
            assertAppearance(openWindow, score, true);
        } finally {
            try {
                if (following[0] != null) {
                    following[0].close();
                }
            } finally {
                try {
                    if (frame != null) {
                        AuditSupport.dispose(frame);
                    }
                } finally {
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
            }
        }
    }

    @Test
    void givenDarkToolbar_whenSystemTurnsLight_thenSamePaintedIconRemainsLegible() throws Exception {
        LookAndFeel[] originalLaf = new LookAndFeel[1];
        Map<Object, Object> originalOverrides = new HashMap<>();
        Theme theme = new Theme();
        FakeSystemAppearance system = new FakeSystemAppearance();
        AutoCloseable[] following = new AutoCloseable[1];
        MainFrame frame = null;
        try {
            SwingUtilities.invokeAndWait(() -> {
                originalLaf[0] = UIManager.getLookAndFeel();
                originalOverrides.putAll(uiOverrides());
                UIManager.put("laf.scaleFactor", 1f);
                system.changeTo(Theme.DARK);
                following[0] = theme.followSystem(system);
            });
            frame = AuditSupport.newFrame(AuditSupport.blankEditor(), theme);
            MainFrame openWindow = frame;
            JButton[] button = new JButton[1];
            JToolBar[] toolbar = new JToolBar[1];
            SwingUtilities.invokeAndWait(() -> {
                toolbar[0] = AuditSupport.findComponent(openWindow.getContentPane(), JToolBar.class);
                assertNotNull(toolbar[0]);
                button[0] = AuditSupport.findButtonByActionName(toolbar[0], Texts.get("menus.file.new"));
                assertNotNull(button[0]);
                assertPaintedIconContrast(button[0], false);
            });

            // The real system listener updates the already-open window, not a new toolbar.
            system.changeTo(Theme.LIGHT);
            SwingUtilities.invokeAndWait(() -> {
                openWindow.validate();
                assertSame(toolbar[0], AuditSupport.findComponent(openWindow.getContentPane(), JToolBar.class));
                assertSame(button[0], AuditSupport.findButtonByActionName(toolbar[0], Texts.get("menus.file.new")));
                assertPaintedIconContrast(button[0], true);
            });
        } finally {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    if (following[0] != null) {
                        try {
                            following[0].close();
                        } catch (Exception exception) {
                            throw new AssertionError("Cannot stop observing the fake system", exception);
                        }
                    }
                });
            } finally {
                try {
                    if (frame != null) {
                        AuditSupport.dispose(frame);
                    }
                } finally {
                    SwingUtilities.invokeAndWait(() -> {
                        uiOverrides().keySet().forEach(key -> UIManager.put(key, null));
                        try {
                            UIManager.setLookAndFeel(originalLaf[0]);
                        } catch (javax.swing.UnsupportedLookAndFeelException exception) {
                            throw new AssertionError("Cannot restore the original LookAndFeel", exception);
                        } finally {
                            originalOverrides.forEach(UIManager::put);
                        }
                    });
                }
            }
        }
    }

    @Test
    void givenDarkPreferences_whenSystemTurnsLight_thenSamePaintedSectionTitleRemainsLegible() throws Exception {
        LookAndFeel originalLaf = UIManager.getLookAndFeel();
        Map<Object, Object> originalOverrides = uiOverrides();
        String originalAnimation = System.getProperty(FlatSystemProperties.ANIMATION);
        FakeSystemAppearance system = new FakeSystemAppearance();
        Theme theme = new Theme();
        AutoCloseable[] following = new AutoCloseable[1];
        MainFrame frame = null;
        JDialog dialog = null;
        try {
            SwingUtilities.invokeAndWait(() -> {
                UIManager.put("laf.scaleFactor", 1f);
                system.changeTo(Theme.DARK);
                following[0] = theme.followSystem(system);
            });
            frame = AuditSupport.newFrame(AuditSupport.blankEditor(), theme);
            MainFrame openWindow = frame;
            dialog = AuditSupport.awaitDialog(() -> {
                JMenuItem preferences = AuditSupport.findMenuItem(openWindow.getJMenuBar(),
                        Texts.get("menus.options.preferences"));
                assertNotNull(preferences);
                preferences.doClick();
            }, 5_000);
            JDialog openDialog = dialog;
            PreferencesPanel[] panel = new PreferencesPanel[1];
            JPanel[] section = new JPanel[1];
            com.gstncaruso.tabpro.ui.dialogs.preferences.Preferences[] values =
                    new com.gstncaruso.tabpro.ui.dialogs.preferences.Preferences[1];
            SwingUtilities.invokeAndWait(() -> {
                panel[0] = AuditSupport.findComponent(openDialog, PreferencesPanel.class);
                assertNotNull(panel[0]);
                AuditSupport.findCheckBox(panel[0], Texts.get("score_dialogs.PreferencesPanel.autoScroll")).doClick();
                values[0] = panel[0].toPreferences();
                section[0] = AuditSupport.findComponents(panel[0], JPanel.class).stream()
                        .filter(candidate -> candidate.getBorder() instanceof CompoundBorder compound
                                && compound.getOutsideBorder() instanceof TitledBorder title
                                && Texts.get("score_dialogs.PreferencesPanel.accessibility").equals(title.getTitle()))
                        .findFirst().orElseThrow();
                assertPaintedSectionTitleContrast(openDialog, section[0], false);
            });

            // Only the system event updates the already-open modal dialog; no test-only refresh.
            system.changeTo(Theme.LIGHT);
            SwingUtilities.invokeAndWait(() -> {
                assertTrue(openDialog.isShowing(), "Preferences must stay open across the system change");
                assertSame(panel[0], AuditSupport.findComponent(openDialog, PreferencesPanel.class));
                assertSame(openDialog, section[0].getTopLevelAncestor());
                assertEquals(values[0], panel[0].toPreferences(), "Unsaved preferences must remain intact");
                assertPaintedSectionTitleContrast(openDialog, section[0], true);
            });
        } finally {
            try {
                if (following[0] != null) {
                    following[0].close();
                }
            } finally {
                try {
                    if (dialog != null) {
                        JDialog toClose = dialog;
                        SwingUtilities.invokeAndWait(toClose::dispose);
                    }
                    if (frame != null) {
                        AuditSupport.dispose(frame);
                    }
                } finally {
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
            }
        }
    }

    private static void assertPaintedSectionTitleContrast(JDialog dialog, JPanel section, boolean light) {
        assertTrue(section.isShowing(), "The existing Preferences section must be visible");
        TitledBorder title = (TitledBorder) ((CompoundBorder) section.getBorder()).getOutsideBorder();
        BufferedImage image = render(dialog.getContentPane());
        Point origin = SwingUtilities.convertPoint(section, 0, 0, dialog.getContentPane());
        // Real section padding, inside its border and clear of its controls; no synthetic opaque backdrop.
        Color background = new Color(image.getRGB((origin.x + 12) * 2,
                (origin.y + section.getHeight() - 12) * 2));
        var metrics = section.getFontMetrics(title.getTitleFont());
        int baseline = title.getBaseline(section, section.getWidth(), section.getHeight());
        assertTrue(baseline >= metrics.getAscent(), "The section title must have a laid-out baseline");
        // Stay strictly inside the title's cleared gap: neither the frame line nor controls can count as text.
        Rectangle glyphs = new Rectangle((origin.x + 16) * 2,
                (origin.y + baseline - metrics.getAscent()) * 2,
                (metrics.stringWidth(title.getTitle()) - 24) * 2, metrics.getAscent() * 2);
        assertTrue(glyphs.width > 0, "The title must provide a measurable glyph region");
        double ratio = strongestContrast(image, background, glyphs);
        String diagnostic = String.format(
                "Existing painted Preferences title '%s' on #%06X has contrast %.3f; minimum %.1f",
                title.getTitle(), background.getRGB() & 0xFFFFFF, ratio, Contrast.TEXT_MINIMUM_RATIO);
        assertAll(diagnostic,
                () -> assertBrightness(background, light, "Preferences section background"),
                () -> assertTrue(ratio >= Contrast.TEXT_MINIMUM_RATIO, diagnostic));
    }

    private static void assertPaintedIconContrast(JButton button, boolean light) {
        assertTrue(button.isShowing() && button.isEnabled(), "The existing toolbar icon must be visible and enabled");
        Component surface = button.getParent();
        while (surface != null && !surface.isOpaque()) {
            surface = surface.getParent();
        }
        assertNotNull(surface, "The transparent toolbar must have a real background");
        BufferedImage image = render(surface);
        Point origin = SwingUtilities.convertPoint(button, 0, 0, surface);
        Color background = new Color(image.getRGB(origin.x * 2, origin.y * 2));
        assertBrightness(background, light, "toolbar behind the existing icon");
        Color stroke = background;
        double ratio = 1;
        // This borderless, textless button paints only its icon over the real toolbar surface.
        for (int y = origin.y * 2; y < (origin.y + button.getHeight()) * 2; y++) {
            for (int x = origin.x * 2; x < (origin.x + button.getWidth()) * 2; x++) {
                Color pixel = new Color(image.getRGB(x, y));
                double contrast = Contrast.ratio(pixel, background);
                if (contrast > ratio) {
                    ratio = contrast;
                    stroke = pixel;
                }
            }
        }
        assertTrue(ratio >= Contrast.GRAPHICAL_MINIMUM_RATIO,
                String.format("Existing painted toolbar icon #%06X on #%06X has contrast %.3f; minimum %.1f",
                        stroke.getRGB() & 0xFFFFFF, background.getRGB() & 0xFFFFFF,
                        ratio, Contrast.GRAPHICAL_MINIMUM_RATIO));
    }

    private static void assertAppearance(MainFrame frame, Score score, boolean light) throws Exception {
        // Drain the event posted by the system listener before inspecting Swing on its EDT.
        SwingUtilities.invokeAndWait(() -> {
            frame.validate();
            MixTable mixer = AuditSupport.findComponent(frame.getContentPane(), MixTable.class);
            ScoreCanvas canvas = AuditSupport.findComponent(frame.getContentPane(), ScoreCanvas.class);
            assertNotNull(mixer);
            assertNotNull(canvas);
            BufferedImage header = render(mixer.getComponent(0));
            BufferedImage notation = render(canvas);
            Color headerBackground = new Color(header.getRGB(header.getWidth() - 4, header.getHeight() / 2));
            Color scoreBackground = new Color(notation.getRGB(0, 0));
            ScoreLayout layout = ScoreLayout.of(score, 1_000_000);
            Rectangle beat = layout.beatBounds(0, 0, 0);
            Track guitar = score.tracks().get(0);
            Note note = new Note(4, 3);
            int noteX = (beat.x + beat.width / 2) * 2;
            int noteY = layout.stepY(0, 0,
                    StaffPosition.of(guitar.tuning().pitchOf(note), Clef.TREBLE).step()) * 2;
            double noteContrast = strongestContrast(notation, scoreBackground,
                    new Rectangle(noteX - 4, noteY - 4, 8, 8));

            assertAll("The same window follows the " + (light ? "light" : "dark") + " system appearance",
                    () -> assertBrightness(headerBackground, light, "mixer header"),
                    () -> assertBrightness(scoreBackground, light, "score background"),
                    () -> assertTrue(noteContrast >= Contrast.TEXT_MINIMUM_RATIO,
                            "Rendered notehead must remain legible; contrast: " + noteContrast));
        });
    }

    private static void assertBrightness(Color background, boolean light, String description) {
        boolean looksLight = Contrast.ratio(background, Color.BLACK) > Contrast.ratio(background, Color.WHITE);
        assertTrue(looksLight == light,
                description + " must follow the system appearance, rendered " + background);
    }

    private static double strongestContrast(BufferedImage image, Color background, Rectangle area) {
        double strongest = 1;
        for (int y = area.y; y < area.y + area.height; y++) {
            for (int x = area.x; x < area.x + area.width; x++) {
                strongest = Math.max(strongest, Contrast.ratio(background, new Color(image.getRGB(x, y))));
            }
        }
        return strongest;
    }

    private static BufferedImage render(Component component) {
        assertTrue(component.getWidth() > 0 && component.getHeight() > 0,
                "The open window must lay out " + component.getClass().getSimpleName());
        BufferedImage image = new BufferedImage(component.getWidth() * 2, component.getHeight() * 2,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.scale(2, 2);
            graphics.setClip(0, 0, component.getWidth(), component.getHeight());
            component.printAll(graphics);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static Map<Object, Object> uiOverrides() {
        Map<Object, Object> overrides = new HashMap<>();
        UIManager.getDefaults().forEach(overrides::put);
        return overrides;
    }

    private static final class FakeSystemAppearance implements SystemAppearance {
        private String current = Theme.LIGHT;
        private final List<Consumer<String>> listeners = new ArrayList<>();

        @Override
        public AutoCloseable observe(Consumer<String> listener) {
            listeners.add(listener);
            listener.accept(current);
            return () -> listeners.remove(listener);
        }

        void changeTo(String appearance) {
            current = appearance;
            List.copyOf(listeners).forEach(listener -> listener.accept(appearance));
        }
    }
}
