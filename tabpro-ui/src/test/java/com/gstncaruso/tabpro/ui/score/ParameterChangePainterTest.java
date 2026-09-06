package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Como se ve un cambio de parametro insertado en medio de la partitura: el
 * rectangulito rojo del manual para lo que no tiene simbolo musical, y la negra
 * con su numero para el cambio de tempo.
 */
class ParameterChangePainterTest {

    private static final int WIDTH = 900;

    @Test
    void aScoreWithoutParameterChangesLeavesTheAirAboveTheStaffAlone() {
        Painted painted = paint(scoreWith(plainMeasure()));

        assertFalse(painted.hasColorAbove(0, 0, 0, ScoreColors.PARAMETER_CHANGE));
    }

    @Test
    void aChangeWithoutAMusicalSymbolIsMarkedWithALittleRedRectangle() {
        Painted painted = paint(scoreWith(changingAt(0, change(SoundParameter.PAN, 20))));

        assertTrue(painted.hasColorAbove(0, 0, 0, ScoreColors.PARAMETER_CHANGE),
                "el cambio de paneo se anuncia con el rectangulito rojo");
    }

    @Test
    void theMarkGoesOverTheBeatThatCarriesTheChange() {
        Painted painted = paint(scoreWith(changingAt(2, change(SoundParameter.PAN, 20))));

        assertTrue(painted.hasColorAbove(0, 0, 2, ScoreColors.PARAMETER_CHANGE));
        assertFalse(painted.hasColorAbove(0, 0, 0, ScoreColors.PARAMETER_CHANGE),
                "los otros beats no llevan nada");
    }

    @Test
    void aTempoChangeIsWrittenAsASymbolAndNeedsNoRectangle() {
        Painted withTempo = paint(scoreWith(changingAt(1, change(SoundParameter.TEMPO, 90))));
        Painted plain = paint(scoreWith(plainMeasure()));

        assertFalse(withTempo.looksLikeAbove(plain, 0, 0, 1),
                "el cambio de tempo se escribe arriba del pentagrama");
        assertFalse(withTempo.hasColorAbove(0, 0, 1, ScoreColors.PARAMETER_CHANGE),
                "el tempo tiene símbolo musical, así que no necesita el rectángulo");
    }

    @Test
    void aChangeThatTouchesTempoAndPanShowsBothThings() {
        ParameterChange both = change(SoundParameter.TEMPO, 90).changing(SoundParameter.PAN, 20);
        Painted painted = paint(scoreWith(changingAt(1, both)));
        Painted panOnly = paint(scoreWith(changingAt(1, change(SoundParameter.PAN, 20))));

        assertTrue(painted.hasColorAbove(0, 0, 1, ScoreColors.PARAMETER_CHANGE));
        assertFalse(painted.looksLikeAbove(panOnly, 0, 0, 1), "además del rectángulo, el tempo dice su número");
    }

    @Test
    void everyTrackShowsTheChangesThatAreWrittenOnIt() {
        Score score = new Score("", 120, List.of(
                guitarWith(plainMeasure()),
                new Track("Bajo", Tuning.standardBass(), Channel.playing(Track.BASS_PROGRAM),
                        List.of(changingAt(0, change(SoundParameter.PAN, 20))))));

        Painted painted = paint(score);

        assertTrue(painted.hasColorAbove(1, 0, 0, ScoreColors.PARAMETER_CHANGE));
        assertFalse(painted.hasColorAbove(0, 0, 0, ScoreColors.PARAMETER_CHANGE));
    }

    // ---- armado de partituras de prueba -----------------------------------

    private static ParameterChange change(SoundParameter parameter, int value) {
        return ParameterChange.nothing().changing(parameter, value);
    }

    private static Measure plainMeasure() {
        return new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(1, 0)),
                Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3)),
                Beat.of(Duration.quarter(), new Note(1, 5))));
    }

    private static Measure changingAt(int beat, ParameterChange change) {
        return plainMeasure().withBeat(beat, plainMeasure().beat(beat)
                .withEffects(BeatEffects.none().withParameterChange(change)));
    }

    private static Track guitarWith(Measure... measures) {
        return new Track("Guitarra", Tuning.standard(), Channel.playing(Track.GUITAR_PROGRAM), List.of(measures));
    }

    private static Score scoreWith(Measure... measures) {
        return new Score("", 120, List.of(guitarWith(measures)));
    }

    private static Painted paint(Score score) {
        ScoreLayout layout = ScoreLayout.of(score, WIDTH);
        BufferedImage image = new BufferedImage(WIDTH, layout.totalHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setClip(0, 0, WIDTH, layout.totalHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        ScorePainter.paint(g, layout, score, new Cursor(0, 0, 0, 1), Playhead.silent());
        g.dispose();
        return new Painted(image, layout);
    }

    private record Painted(BufferedImage image, ScoreLayout layout) {

        boolean hasColorAbove(int track, int measure, int beat, Color color) {
            return scanAbove(track, measure, beat, pixel -> pixel == color.getRGB());
        }

        /** Si el aire sobre ese beat quedo dibujado igual que en la otra hoja. */
        boolean looksLikeAbove(Painted other, int track, int measure, int beat) {
            Rectangle bounds = layout.beatBounds(track, measure, beat);
            int staffTop = layout.staffTop(track, measure);
            for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
                for (int y = staffTop - ScoreLayout.STAFF_HEADROOM; y < staffTop; y++) {
                    if (isInside(x, y) && image.getRGB(x, y) != other.image.getRGB(x, y)) {
                        return false;
                    }
                }
            }
            return true;
        }

        /** La franja de aire que hay entre la etiqueta de la pista y el pentagrama, sobre ese beat. */
        private boolean scanAbove(int track, int measure, int beat, java.util.function.IntPredicate wanted) {
            Rectangle bounds = layout.beatBounds(track, measure, beat);
            int staffTop = layout.staffTop(track, measure);
            for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
                for (int y = staffTop - ScoreLayout.STAFF_HEADROOM; y < staffTop; y++) {
                    if (isInside(x, y) && wanted.test(image.getRGB(x, y))) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isInside(int x, int y) {
            return x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight();
        }
    }
}
