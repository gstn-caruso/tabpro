package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.Selection;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.LyricLine;
import com.gstncaruso.tabpro.core.model.Lyrics;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import com.gstncaruso.tabpro.ui.score.ScoreLayout;
import com.gstncaruso.tabpro.ui.score.ScorePainter;
import com.gstncaruso.tabpro.ui.score.ViewMode;
import com.gstncaruso.tabpro.ui.score.VisibleNotations;
import com.gstncaruso.tabpro.ui.score.VisibleTracks;
import com.gstncaruso.tabpro.ui.status.MeasureCompleteness;
import com.gstncaruso.tabpro.ui.status.StatusBar;
import com.gstncaruso.tabpro.ui.theme.Contrast;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.JLabel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/** Isolated because applying a real theme changes JVM-wide Swing defaults. */
@Isolated
class ScoreThemeTest {

    @Test
    void givenScore_whenLightThemeApplied_thenStaffIsLegibleOnLightBackground() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LookAndFeel originalLaf = UIManager.getLookAndFeel();
            Map<Object, Object> originalOverrides = uiOverrides();
            try {
                // At 2x, a one-unit staff stroke has fully covered pixels, not only antialiased edges.
                UIManager.put("laf.scaleFactor", 2f);
                Note note = new Note(4, 3); // F in a staff space, so a line cannot stand in for its notehead.
                Measure measure = new Measure(new TimeSignature(1, 4), List.of(Beat.of(Duration.quarter(), note)));
                Track guitar = Track.standardGuitar("Guitarra").withMeasures(List.of(measure));
                Score score = new Score("Light score", 120, List.of(guitar));
                ScoreCanvas canvas = new ScoreCanvas(new Editor(score));
                canvas.setViewMode(ViewMode.SCREEN_HORIZONTAL);

                new Theme().apply(Theme.LIGHT);
                canvas.setSize(canvas.getPreferredSize());
                BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                try {
                    graphics.setClip(0, 0, image.getWidth(), image.getHeight());
                    canvas.paint(graphics);
                } finally {
                    graphics.dispose();
                }

                Color background = new Color(image.getRGB(0, 0));
                ScoreLayout layout = ScoreLayout.of(score, 1_000_000);
                Rectangle beat = layout.beatBounds(0, 0, 0);
                int noteX = (beat.x + beat.width / 2) * 2;
                int noteY = layout.stepY(0, 0, StaffPosition.of(guitar.tuning().pitchOf(note), Clef.TREBLE).step()) * 2;
                double noteContrast = strongestContrast(image, background, new Rectangle(noteX - 4, noteY - 4, 8, 8));

                assertAll("The rendered light score has a light background and readable notation",
                        () -> assertTrue(Contrast.ratio(background, Color.BLACK) > Contrast.ratio(background, Color.WHITE),
                                "Light theme must paint a light score background, but rendered " + background),
                        () -> assertTrue(noteContrast >= Contrast.TEXT_MINIMUM_RATIO,
                                "Rendered notehead contrast: " + noteContrast),
                        () -> {
                            int lineX = (layout.measureX(0) + layout.measureWidth(0) - 8) * 2;
                            for (int line = 0; line < 5; line++) {
                                int lineY = layout.staffLineY(0, 0, line) * 2;
                                double lineContrast = strongestContrast(image, background, new Rectangle(lineX, lineY - 1, 1, 3));
                                assertTrue(lineContrast >= Contrast.GRAPHICAL_MINIMUM_RATIO,
                                        "Rendered staff line " + line + " contrast: " + lineContrast);
                            }
                        });
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

    @Test
    void givenAnnotatedScore_whenLightThemeApplied_thenAnnotationsRemainLegible() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LookAndFeel originalLaf = UIManager.getLookAndFeel();
            Map<Object, Object> originalOverrides = uiOverrides();
            try {
                // A small real score, with independent regions for each annotation family.
                // Both pitches lie in staff spaces; the crops exclude staff lines and stems.
                Note melody = new Note(3, 2); // Written A4.
                Note otherVoice = new Note(4, 3); // Written F4.
                Measure annotated = new Measure(TimeSignature.fourFour(), List.of(
                        Beat.of(Duration.quarter(), melody.withDynamic(Dynamic.PIANO)),
                        Beat.of(Duration.quarter(), melody.withDynamic(Dynamic.FORTISSIMO)).withEffects(
                                BeatEffects.none().withChord(ChordDiagram.named("Do", List.of(-1, 3, 2, 0, 1, 0)))),
                        Beat.of(Duration.quarter(), melody.toggling(Ornament.LET_RING)),
                        Beat.of(Duration.quarter(), melody).withEffects(BeatEffects.none().withText("dolce"))))
                        .withVoice(VoicePart.BASS, new Voice(List.of(
                                Beat.of(Duration.quarter(), otherVoice), Beat.of(Duration.quarter(), otherVoice),
                                Beat.rest(Duration.quarter().longer()))))
                        .mappingAttributes(attributes -> attributes.withRepeatOpen(true).withRepeatCount(3));
                Measure drums = new Measure(TimeSignature.fourFour(), List.of(
                        Beat.of(Duration.quarter(), new Note(1, 42)),
                        Beat.of(Duration.quarter(), new Note(2, 38)),
                        Beat.of(Duration.quarter(), new Note(6, 36)),
                        Beat.of(Duration.quarter(), new Note(3, 56))));
                Measure quiet = new Measure(TimeSignature.fourFour(), List.of(
                        Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter()),
                        Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));
                // The guitar is second so the chord name has room above its staff.
                Track guitar = Track.standardGuitar("Guitarra").withMeasures(List.of(annotated, quiet));
                Score score = new Score("Annotations", 120, List.of(
                        Track.percussion("Batería").withMeasures(List.of(drums, quiet)), guitar))
                        .withLyrics(Lyrics.none().onTrack(1).withLine(0, LyricLine.empty().startingAt(1).saying("Luz")));
                ScoreLayout layout = ScoreLayout.of(score, 1000, VisibleTracks.all(), VisibleNotations.both(), true);
                BufferedImage image = new BufferedImage(2000, layout.totalHeight() * 2, BufferedImage.TYPE_INT_RGB);
                new Theme().apply(Theme.LIGHT);
                Graphics2D graphics = image.createGraphics();
                try {
                    graphics.scale(2, 2); // Fully covered glyph/stroke pixels rather than just antialiased edges.
                    graphics.setClip(0, 0, 1000, layout.totalHeight());
                    ScorePainter.paint(graphics, layout, score, new Cursor(-1, 0, 0, 1), Playhead.silent(),
                            Optional.of(Selection.ofMeasures(0, 1, 1)));
                } finally {
                    graphics.dispose();
                }

                Color background = new Color(image.getRGB(0, 0));
                int firstX = centerX(layout.beatBounds(1, 0, 0));
                int chordX = centerX(layout.beatBounds(1, 0, 1));
                int effectX = centerX(layout.beatBounds(1, 0, 2));
                int textX = centerX(layout.beatBounds(1, 0, 3));
                int melodyY = layout.stepY(1, 0, StaffPosition.of(guitar.tuning().pitchOf(melody), Clef.TREBLE).step());
                int otherY = layout.stepY(1, 0, StaffPosition.of(guitar.tuning().pitchOf(otherVoice), Clef.TREBLE).step());
                int staffTop = layout.staffTop(1, 0);
                int tabTop = layout.tabTop(1, 0);
                int right = layout.measureX(0) + layout.measureWidth(0);
                Rectangle selected = layout.measureBounds(0, 1);
                int drumX = centerX(layout.beatBounds(0, 0, 0));
                int drumY = layout.stringY(0, 0, 1);
                Color drumBackground = new Color(image.getRGB((drumX - 5) * 2, (drumY - 5) * 2));

                assertAll("Light score annotations remain readable independently of notes and lines",
                        () -> assertTrue(Contrast.ratio(background, Color.BLACK) > Contrast.ratio(background, Color.WHITE),
                                "The rendered score must have a light background: " + background),
                        () -> assertReadable(image, background, "Piano dynamic notehead",
                                new Rectangle(firstX - 2, melodyY - 2, 4, 4), Contrast.GRAPHICAL_MINIMUM_RATIO),
                        () -> assertReadable(image, background, "Fortissimo dynamic notehead",
                                new Rectangle(chordX - 2, melodyY - 2, 4, 4), Contrast.GRAPHICAL_MINIMUM_RATIO),
                        () -> assertReadable(image, background, "Inactive voice notehead",
                                new Rectangle(firstX - 2, otherY - 2, 4, 4), Contrast.GRAPHICAL_MINIMUM_RATIO),
                        () -> assertReadable(image, background, "Chord name (above the diagram)",
                                new Rectangle(chordX - 16, staffTop - 70, 32, 14), Contrast.TEXT_MINIMUM_RATIO),
                        () -> assertReadable(image, background, "Lyric syllable (below the tablature)",
                                new Rectangle(firstX - 16, layout.tabBottom(1, 0) + 2, 32, 12), Contrast.TEXT_MINIMUM_RATIO),
                        () -> assertReadable(image, background, "Let-ring tablature effect",
                                new Rectangle(effectX - 20, tabTop - 19, 40, 12), Contrast.TEXT_MINIMUM_RATIO),
                        () -> assertReadable(image, background, "Free-text annotation",
                                new Rectangle(textX - 20, tabTop - 19, 40, 12), Contrast.TEXT_MINIMUM_RATIO),
                        () -> assertReadable(image, background, "Repeat count (above the bar)",
                                new Rectangle(right - 25, staffTop - 16, 14, 13), Contrast.TEXT_MINIMUM_RATIO),
                        () -> assertReadable(image, background, "Repeat bracket (between staff and tablature)",
                                new Rectangle(right - 4, layout.staffBottom(1, 0) + 6, 2, 8), Contrast.GRAPHICAL_MINIMUM_RATIO),
                        () -> assertReadable(image, background, "Tempo text",
                                new Rectangle(drumX, layout.staffTop(0, 0) - 42, 24, 12), Contrast.TEXT_MINIMUM_RATIO),
                        () -> assertTrue(Contrast.ratio(drumBackground, Color.BLACK) > Contrast.ratio(drumBackground, Color.WHITE),
                                "Percussion note must not leave a dark patch on the light score: " + drumBackground),
                        () -> assertReadable(image, drumBackground, "Percussion cross against its local background",
                                new Rectangle(drumX - 2, drumY - 2, 4, 4), Contrast.GRAPHICAL_MINIMUM_RATIO),
                        () -> assertReadable(image, background, "Selection border (away from staff/tab lines)",
                                new Rectangle(selected.x + selected.width - 2, layout.staffBottom(0, 1) + 6, 2, 8),
                                Contrast.GRAPHICAL_MINIMUM_RATIO));
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

    @Test
    void givenShortMeasure_whenLightThemeApplied_thenWarningNumberRemainsLegible() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LookAndFeel originalLaf = UIManager.getLookAndFeel();
            Map<Object, Object> originalOverrides = uiOverrides();
            try {
                Measure shortMeasure = new Measure(TimeSignature.fourFour(),
                        List.of(Beat.of(Duration.quarter(), new Note(4, 3))));
                Score score = new Score("Short measure", 120,
                        List.of(Track.standardGuitar("Guitarra").withMeasures(List.of(shortMeasure))));
                ScoreLayout layout = ScoreLayout.of(score, 1000);
                BufferedImage image = new BufferedImage(2000, layout.totalHeight() * 2, BufferedImage.TYPE_INT_RGB);
                new Theme().apply(Theme.LIGHT);
                Graphics2D graphics = image.createGraphics();
                try {
                    graphics.scale(2, 2); // Fully covered glyph pixels, not only antialiased edges.
                    graphics.setClip(0, 0, 1000, layout.totalHeight());
                    ScorePainter.paint(graphics, layout, score, new Cursor(-1, 0, 0, 1), Playhead.silent());
                } finally {
                    graphics.dispose();
                }

                // Only the warning number above the staff: no track name, tempo, lines or cursor.
                Rectangle number = new Rectangle(layout.measureX(0) + 2, layout.staffTop(0, 0) - 18, 12, 14);
                Color background = new Color(image.getRGB(number.x * 2, number.y * 2));
                assertTrue(Contrast.ratio(background, Color.BLACK) > Contrast.ratio(background, Color.WHITE),
                        "The rendered warning must have a light background: " + background);
                assertReadable(image, background, "Short-measure warning number on " + background,
                        number, Contrast.TEXT_MINIMUM_RATIO);
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

    @Test
    void givenShortMeasure_whenStatusWarningChangesToLong_thenTextRemainsLegibleInBothThemes() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LookAndFeel originalLaf = UIManager.getLookAndFeel();
            Map<Object, Object> originalOverrides = uiOverrides();
            try {
                UIManager.put("laf.scaleFactor", 1f);
                Theme theme = new Theme();
                for (String appearance : List.of(Theme.LIGHT, Theme.DARK)) {
                    theme.apply(appearance);
                    Measure shortMeasure = new Measure(TimeSignature.fourFour(),
                            List.of(Beat.rest(Duration.quarter())));
                    Editor editor = new Editor(new Score("Warning", 120,
                            List.of(Track.standardGuitar("Guitarra").withMeasures(List.of(shortMeasure)))));
                    StatusBar bar = new StatusBar(editor);
                    JLabel warning = findLabel(bar, Texts.get("views.StatusBar.measureStatus"));
                    assertNotNull(warning, "The real status bar must expose its measure warning");

                    assertEquals(Texts.get("views.StatusBar.measureText", MeasureCompleteness.TOO_SHORT.label()),
                            warning.getText(), appearance + " short-measure warning");
                    assertStatusWarningReadable(bar, warning, appearance);

                    editor.setNoteValue(NoteValue.WHOLE);
                    editor.toggleDot(); // Six beats in 4/4: the same indicator now warns of a long measure.

                    assertEquals(Texts.get("views.StatusBar.measureText", MeasureCompleteness.TOO_LONG.label()),
                            warning.getText(), appearance + " long-measure warning after editing");
                    assertStatusWarningReadable(bar, warning, appearance);
                }
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

    private static void assertStatusWarningReadable(StatusBar bar, JLabel warning, String appearance) {
        bar.setSize(bar.getPreferredSize());
        layout(bar);
        BufferedImage image = new BufferedImage(bar.getWidth() * 2, bar.getHeight() * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.scale(2, 2); // Fully covered glyph pixels, not only antialiased edges.
            bar.paint(graphics); // Paint ancestors too: the label and its sunken panel are transparent.
        } finally {
            graphics.dispose();
        }
        Rectangle text = SwingUtilities.convertRectangle(warning.getParent(), warning.getBounds(), bar);
        // Sample the panel's empty inner padding, not its bevel or an uncomposited label background.
        Color background = new Color(image.getRGB((text.x - 2) * 2, (text.y + text.height / 2) * 2));
        boolean isLight = Contrast.ratio(background, Color.BLACK) > Contrast.ratio(background, Color.WHITE);
        assertEquals(Theme.LIGHT.equals(appearance), isLight,
                appearance + " warning background must follow the theme: " + background);
        assertReadable(image, background, appearance + " rendered '" + warning.getText() + "'",
                text, Contrast.TEXT_MINIMUM_RATIO);
    }

    private static JLabel findLabel(Container root, String accessibleName) {
        for (Component child : root.getComponents()) {
            if (child instanceof JLabel label
                    && accessibleName.equals(label.getAccessibleContext().getAccessibleName())) {
                return label;
            }
            if (child instanceof Container nested) {
                JLabel found = findLabel(nested, accessibleName);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container nested) layout(nested);
        }
    }

    private static int centerX(Rectangle beat) {
        return beat.x + beat.width / 2;
    }

    private static void assertReadable(BufferedImage image, Color background, String label, Rectangle area, double minimum) {
        Rectangle pixels = new Rectangle(area.x * 2, area.y * 2, area.width * 2, area.height * 2);
        double contrast = strongestContrast(image, background, pixels);
        assertTrue(contrast >= minimum, label + " contrast: " + contrast + " (minimum " + minimum + ")");
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

    private static Map<Object, Object> uiOverrides() {
        Map<Object, Object> overrides = new HashMap<>();
        // Hashtable.forEach visits developer overrides, not the merged LookAndFeel defaults.
        UIManager.getDefaults().forEach(overrides::put);
        return overrides;
    }
}
