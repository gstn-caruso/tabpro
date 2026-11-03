package com.gstncaruso.tabpro.app.audit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.formdev.flatlaf.FlatSystemProperties;
import com.gstncaruso.tabpro.app.Theme;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.instruments.BeatViews;
import com.gstncaruso.tabpro.ui.instruments.FretboardDisplayMode;
import com.gstncaruso.tabpro.ui.instruments.KeyboardDisplayMode;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import com.gstncaruso.tabpro.ui.score.ScoreLayout;
import com.gstncaruso.tabpro.ui.score.ViewMode;
import com.gstncaruso.tabpro.ui.status.StatusBar;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import com.gstncaruso.tabpro.ui.tracks.MarkerZone;
import com.gstncaruso.tabpro.ui.tracks.MeasureGrid;
import com.gstncaruso.tabpro.ui.tracks.MixTable;
import com.gstncaruso.tabpro.ui.tracks.MixTableRow;
import com.gstncaruso.tabpro.ui.tracks.TrackPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Isolated;

/** Real JFrame, isolated because changing its theme changes JVM-wide Swing defaults. */
@Tag("integration")
@Isolated
class OpenWindowThemeAuditTest {

    @Test
    void givenOpenMixer_whenLightThemeApplied_thenExistingSurfaceIsLegible() throws Exception {
        LookAndFeel originalLaf = UIManager.getLookAndFeel();
        Map<Object, Object> originalOverrides = uiOverrides();
        String originalAnimation = System.getProperty(FlatSystemProperties.ANIMATION);
        Theme theme = new Theme();
        MainFrame frame = null;
        try {
            SwingUtilities.invokeAndWait(() -> {
                UIManager.put("laf.scaleFactor", 1f);
                theme.apply(Theme.DARK);
            });
            Measure measure = new Measure(new TimeSignature(1, 4),
                    List.of(Beat.of(Duration.quarter(), new Note(4, 3))));
            Score score = new Score("Theme transition", 120,
                    List.of(Track.standardGuitar("Guitarra").withMeasures(List.of(measure))));
            frame = AuditSupport.newFrame(new Editor(score), theme);
            MainFrame openWindow = frame;
            SwingUtilities.invokeAndWait(() -> {
                ScoreCanvas canvas = AuditSupport.findComponent(openWindow.getContentPane(), ScoreCanvas.class);
                canvas.setViewMode(ViewMode.SCREEN_HORIZONTAL);
                assertAll("The same open window follows both theme transitions",
                        () -> applyAndAssertLegible(openWindow, score, Theme.LIGHT),
                        () -> applyAndAssertLegible(openWindow, score, Theme.DARK));
            });
        } finally {
            if (frame != null) {
                AuditSupport.dispose(frame);
            }
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

    @Test
    void givenOpenAuxiliaryPanels_whenThemeChanges_thenSurfacesAndLabelsFollowAppearance() throws Exception {
        LookAndFeel originalLaf = UIManager.getLookAndFeel();
        Map<Object, Object> originalOverrides = uiOverrides();
        String originalAnimation = System.getProperty(FlatSystemProperties.ANIMATION);
        Theme theme = new Theme();
        MainFrame frame = null;
        try {
            SwingUtilities.invokeAndWait(() -> {
                UIManager.put("laf.scaleFactor", 1f);
                theme.apply(Theme.DARK);
            });
            Editor editor = AuditSupport.blankEditor();
            Score score = editor.score();
            frame = AuditSupport.newFrame(editor, theme);
            MainFrame openWindow = frame;
            SwingUtilities.invokeAndWait(() -> {
                Container root = openWindow.getContentPane();
                BeatViews views = AuditSupport.findComponent(root, BeatViews.class);
                TrackPanel tracks = AuditSupport.findComponent(root, TrackPanel.class);
                var fretboard = views.fretboard();
                var keyboard = views.keyboard();
                views.setFretboardVisible(true);
                views.setKeyboardVisible(true);
                tracks.setVisible(true);
                JComboBox<?> display = AuditSupport.findComponents(views, JComboBox.class).stream()
                        .filter(combo -> combo.getItemAt(0) instanceof FretboardDisplayMode)
                        .findFirst().orElseThrow();
                display.setSelectedItem(FretboardDisplayMode.BEAT_AND_MEASURE);
                keyboard.setDisplayMode(KeyboardDisplayMode.BEAT_AND_SCALE);
                var cursor = editor.cursor();
                openWindow.pack();

                List<Executable> transitions = new ArrayList<>();
                for (String appearance : List.of(Theme.LIGHT, Theme.DARK)) {
                    transitions.add(() -> {
                        JMenuItem action = AuditSupport.findMenuItem(openWindow.getJMenuBar(),
                                Texts.get("window.Theme." + appearance));
                        assertNotNull(action, "The open window exposes the " + appearance + " theme action");
                        action.doClick();
                        openWindow.validate();
                        boolean light = Theme.LIGHT.equals(appearance);
                        List<Executable> checks = new ArrayList<>();
                        checks.add(() -> assertSame(views, AuditSupport.findComponent(root, BeatViews.class)));
                        checks.add(() -> assertSame(tracks, AuditSupport.findComponent(root, TrackPanel.class)));
                        checks.add(() -> assertSame(fretboard, views.fretboard()));
                        checks.add(() -> assertSame(keyboard, views.keyboard()));
                        checks.add(() -> assertTrue(views.isShowing() && tracks.isShowing()
                                && fretboard.isShowing() && keyboard.isShowing(), "Auxiliary panels remain open"));
                        checks.add(() -> assertSame(score, editor.score(), "Theme changes do not edit the score"));
                        checks.add(() -> assertEquals(cursor, editor.cursor()));
                        checks.add(() -> assertSame(display,
                                AuditSupport.findComponents(views, JComboBox.class).stream()
                                        .filter(combo -> combo.getItemAt(0) instanceof FretboardDisplayMode)
                                        .findFirst().orElseThrow()));
                        checks.add(() -> assertEquals(FretboardDisplayMode.BEAT_AND_MEASURE,
                                display.getSelectedItem()));
                        checks.add(() -> assertEquals(FretboardDisplayMode.BEAT_AND_MEASURE, fretboard.displayMode()));
                        checks.add(() -> assertEquals(KeyboardDisplayMode.BEAT_AND_SCALE, keyboard.displayMode()));
                        // Instrument margins, not the fretboard wood, keys or musical marks.
                        checks.add(() -> assertNeutralPadding(fretboard, light, "fretboard margin"));
                        checks.add(() -> assertNeutralPadding(keyboard, light, "keyboard margin"));
                        for (JViewport viewport : AuditSupport.findComponents(tracks, JViewport.class)) {
                            checks.add(() -> assertSurface(viewport, light, "mixer/global-view viewport"));
                        }
                        List<JLabel> labels = new ArrayList<>(AuditSupport.findComponents(views, JLabel.class));
                        for (JToolBar toolbar : AuditSupport.findComponents(root, JToolBar.class)) {
                            if (toolbar.isShowing()) {
                                checks.add(() -> assertNeutralPadding(toolbar, light, "toolbar padding"));
                                labels.addAll(AuditSupport.findComponents(toolbar, JLabel.class));
                            }
                        }
                        for (JLabel label : labels) {
                            if (label.isShowing() && label.getText() != null && !label.getText().isBlank()) {
                                checks.add(() -> assertLabelContrast(label));
                                if (SwingUtilities.isDescendingFrom(label, views)) {
                                    checks.add(() -> assertNeutralPadding(label.getParent(), light,
                                            "auxiliary title '" + label.getText() + "'"));
                                }
                            }
                        }
                        assertAll(appearance + " auxiliary surfaces, labels and preserved state", checks);
                    });
                }
                assertAll("The same auxiliary panels follow light and dark", transitions);
            });
        } finally {
            if (frame != null) {
                AuditSupport.dispose(frame);
            }
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

    private static void assertNeutralPadding(Component component, boolean light, String description) {
        Component surface = component;
        while (surface != null && !surface.isOpaque()) {
            surface = surface.getParent();
        }
        assertNotNull(surface, "An opaque ancestor must supply " + description);
        BufferedImage image = render(surface);
        Point padding = SwingUtilities.convertPoint(component, 1, 1, surface);
        Color background = new Color(image.getRGB(padding.x * 2, padding.y * 2));
        boolean looksLight = Contrast.ratio(background, Color.BLACK) > Contrast.ratio(background, Color.WHITE);
        assertTrue(looksLight == light,
                description + " must follow " + (light ? "light" : "dark") + ", rendered " + background);
    }

    private static void applyAndAssertLegible(MainFrame frame, Score score, String theme) {
        JMenuItem menuItem = AuditSupport.findMenuItem(frame.getJMenuBar(), Texts.get("window.Theme." + theme));
        assertNotNull(menuItem, "The real window exposes the " + theme + " theme action");
        menuItem.doClick(); // Includes MainFrame's real component-tree update, not a test-only refresh.
        frame.validate();

        Container root = frame.getContentPane();
        MixTable mixer = AuditSupport.findComponent(root, MixTable.class);
        MixTableRow row = AuditSupport.findComponent(mixer, MixTableRow.class);
        StatusBar status = AuditSupport.findComponent(root, StatusBar.class);
        ScoreCanvas canvas = AuditSupport.findComponent(root, ScoreCanvas.class);
        boolean light = Theme.LIGHT.equals(theme);
        List<Executable> checks = new ArrayList<>();
        checks.add(() -> assertSurface(mixer.getComponent(0), light, "mixer header"));
        checks.add(() -> assertSurface(row, light, "selected mixer row"));
        checks.add(() -> assertSurface(AuditSupport.findComponent(root, MarkerZone.class), light, "marker strip"));
        checks.add(() -> assertSurface(AuditSupport.findComponent(root, MeasureGrid.class), light, "measure map"));
        checks.add(() -> assertSurface(status, light, "status bar"));
        checks.add(() -> assertSurface(canvas, light, "score canvas"));
        checks.add(() -> assertSurface(canvas.getParent(), light, "score viewport"));

        List<JLabel> labels = new ArrayList<>(AuditSupport.findComponents(mixer, JLabel.class));
        labels.addAll(AuditSupport.findComponents(status, JLabel.class));
        labels.addAll(AuditSupport.findComponents(root, JLabel.class).stream()
                .filter(label -> (Texts.get("window.MainFrame.tempo") + " ").equals(label.getText())).toList());
        for (JLabel label : labels) {
            if (label.getText() != null && !label.getText().isBlank()) {
                checks.add(() -> assertLabelContrast(label));
            }
        }
        checks.add(() -> assertMeasureNumberContrast(canvas, score));
        assertAll(theme + " surfaces and notation in the already-open window", checks);
    }

    private static void assertSurface(Component component, boolean light, String description) {
        BufferedImage image = render(component);
        // Right-hand padding is clear of controls, measure cells and the track-color stripe.
        Color background = new Color(image.getRGB(image.getWidth() - 4, image.getHeight() / 2));
        boolean looksLight = Contrast.ratio(background, Color.BLACK) > Contrast.ratio(background, Color.WHITE);
        assertTrue(looksLight == light,
                description + " must follow " + (light ? "light" : "dark") + ", rendered " + background);
    }

    private static void assertLabelContrast(JLabel label) {
        // Compose transparent parents over their opaque ancestor, as in the real window.
        Component surface = label.getParent();
        while (surface != null && !surface.isOpaque()) {
            surface = surface.getParent();
        }
        assertNotNull(surface, "The open window must provide an opaque surface behind " + label.getText());
        BufferedImage composed = render(surface);
        Point padding = SwingUtilities.convertPoint(label, 0, 0, surface);
        Color background = new Color(composed.getRGB(padding.x * 2, padding.y * 2));
        double ratio = Contrast.ratio(label.getForeground(), background);
        assertTrue(ratio >= Contrast.TEXT_MINIMUM_RATIO,
                "Existing label '" + label.getText() + "' contrast: " + ratio + " on " + background);
    }

    private static void assertMeasureNumberContrast(ScoreCanvas canvas, Score score) {
        BufferedImage image = render(canvas);
        Color background = new Color(image.getRGB(0, 0));
        ScoreLayout layout = ScoreLayout.of(score, 1_000_000);
        Rectangle number = new Rectangle((layout.measureX(0) + 2) * 2,
                (layout.staffTop(0, 0) - 18) * 2, 12 * 2, 14 * 2);
        double strongest = 1;
        for (int y = number.y; y < number.y + number.height; y++) {
            for (int x = number.x; x < number.x + number.width; x++) {
                strongest = Math.max(strongest, Contrast.ratio(background, new Color(image.getRGB(x, y))));
            }
        }
        assertTrue(strongest >= Contrast.TEXT_MINIMUM_RATIO,
                "Rendered measure number contrast: " + strongest + " on " + background);
    }

    private static BufferedImage render(Component component) {
        assertTrue(component.getWidth() > 0 && component.getHeight() > 0,
                "The open window must lay out " + component.getClass().getSimpleName());
        BufferedImage image = new BufferedImage(component.getWidth() * 2, component.getHeight() * 2,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.scale(2, 2); // Full glyph pixels without changing the application's UI scale.
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
}
