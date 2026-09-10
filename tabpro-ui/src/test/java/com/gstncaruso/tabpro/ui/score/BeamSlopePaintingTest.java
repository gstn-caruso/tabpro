package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BeamSlopePaintingTest {

    private static final int WIDTH = 900;
    private static final double SPACE = ScoreLayout.STAFF_LINE_SPACING;
    private static final double NOTE_WIDTH = SPACE * 1.28;
    private static final double MAX_BEAM_SLOPE = SPACE;
    private static final double BEAM_GAP = SPACE * 0.84;

    private static final Note LOW = new Note(3, 0);
    private static final Note HIGHER = new Note(3, 2);
    private static final Note VERY_LOW = new Note(4, 0);

    @Test
    void anAscendingGroupSlopesTheBeamUpward() {
        Painted painted = paintEighths(false, LOW, HIGHER);

        int firstTop = painted.topInkY(1, true);
        int lastTop = painted.topInkY(2, true);

        assertTrue(lastTop < firstTop,
                "un grupo ascendente tiene que subir: el y de la ultima nota tiene que ser menor que el de la primera");
    }

    @Test
    void aDescendingGroupSlopesTheBeamDownward() {
        Painted painted = paintEighths(false, HIGHER, LOW);

        int firstTop = painted.topInkY(1, true);
        int lastTop = painted.topInkY(2, true);

        assertTrue(lastTop > firstTop,
                "un grupo descendente tiene que bajar: el y de la ultima nota tiene que ser mayor que el de la primera");
    }

    @Test
    void aGroupWithTheSamePitchKeepsTheBeamHorizontal() {
        Painted painted = paintEighths(false, LOW, LOW);

        assertEquals(painted.topInkY(1, true), painted.topInkY(2, true),
                "notas a la misma altura: la barra sigue horizontal");
    }

    @Test
    void aZigzagGroupFlattensTheBeam() {
        Painted painted = paintSixteenths(LOW, HIGHER, LOW);

        assertEquals(painted.topInkY(1, true), painted.topInkY(3, true),
                "el grupo en zigzag tiene que aplanarse: los extremos quedan a la misma altura");
    }

    @Test
    void theSlopeIsCappedOnAWideInterval() {
        Painted painted = paintEighths(false, VERY_LOW, LOW);

        int firstTop = painted.topInkY(1, true);
        int lastTop = painted.topInkY(2, true);

        assertEquals(MAX_BEAM_SLOPE, firstTop - lastTop, 0.6,
                "la pendiente tiene que quedar acotada al tope aunque el intervalo entre las notas sea mas grande");
    }

    @Test
    void theStemStretchesWithoutGapsFromTheNoteheadUpToTheSlopedBeam() {
        Painted painted = paintEighths(false, VERY_LOW, LOW);

        int beamTop = painted.topInkY(1, true);
        int noteheadY = painted.noteheadY(VERY_LOW);

        assertTrue(painted.hasContinuousInk(painted.stemX(1, true), beamTop, noteheadY),
                "la plica tiene que llegar sin cortes desde la cabeza de la nota hasta la barra inclinada");
    }

    @Test
    void theSecondaryBeamStaysParallelToThePrimaryOne() {
        Painted painted = paintSixteenths(LOW, LOW, HIGHER, HIGHER);

        int primaryFirst = painted.topInkY(1, true);
        int primaryLast = painted.topInkY(4, true);
        assertTrue(primaryLast < primaryFirst, "el grupo es ascendente: la barra primaria tiene que subir");

        assertTrue(painted.hasInkNear(painted.stemX(1, true), (int) Math.round(primaryFirst + BEAM_GAP), 2),
                "la barra secundaria tiene que acompañar a la primaria en el primer extremo");
        assertTrue(painted.hasInkNear(painted.stemX(4, true), (int) Math.round(primaryLast + BEAM_GAP), 2),
                "la barra secundaria tiene que acompañar a la primaria en el ultimo extremo, con la misma pendiente");
    }

    @Test
    void forcingHorizontalBeamsOnTheTrackFlattensAnAscendingGroup() {
        Painted painted = paintEighths(true, LOW, HIGHER);

        assertEquals(painted.topInkY(1, true), painted.topInkY(2, true),
                "con 'Forzar barras horizontales' activo, un grupo ascendente tiene que quedar horizontal");
    }

    private static Painted paintEighths(boolean forceHorizontalBeams, Note first, Note second) {
        Duration eighth = new Duration(NoteValue.EIGHTH, false);
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.quarter()),
                Beat.of(eighth, first), Beat.of(eighth, second),
                Beat.rest(Duration.quarter()), Beat.rest(Duration.quarter())));
        return paint(measure, forceHorizontalBeams);
    }

    private static Painted paintSixteenths(Note... notes) {
        Duration sixteenth = new Duration(NoteValue.SIXTEENTH, false);
        List<Beat> beats = new ArrayList<>();
        beats.add(Beat.rest(Duration.quarter()));
        for (Note note : notes) {
            beats.add(Beat.of(sixteenth, note));
        }
        while (beats.size() < 5) {
            beats.add(Beat.rest(sixteenth));
        }
        beats.add(Beat.rest(Duration.quarter()));
        beats.add(Beat.rest(Duration.quarter()));
        Measure measure = new Measure(TimeSignature.fourFour(), List.copyOf(beats));
        return paint(measure, false);
    }

    private static Painted paint(Measure measure, boolean forceHorizontalBeams) {
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        if (forceHorizontalBeams) {
            track = track.mappingSettings(
                    settings -> settings.withDisplay(settings.display().withForceHorizontalBeams(true)));
        }
        Score score = new Score("", 120, List.of(track));

        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        BufferedImage image = new BufferedImage(WIDTH, layout.totalHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setClip(0, 0, WIDTH, layout.totalHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        ScorePainter.paint(g, layout, score, new Cursor(-1, 0, 0, 1), Playhead.silent());
        g.dispose();
        return new Painted(image, layout);
    }

    private record Painted(BufferedImage image, ScoreLayout layout) {

        int noteheadY(Note note) {
            int step = StaffPosition.of(Tuning.standard().pitchOf(note), Clef.TREBLE).step();
            return layout.stepY(0, 0, step);
        }

        int stemX(int beatIndex, boolean up) {
            Rectangle beat = layout.beatBounds(0, 0, beatIndex);
            double centerX = beat.x + beat.width / 2.0;
            return (int) Math.round(up ? centerX + NOTE_WIDTH / 2 - 0.8 : centerX - NOTE_WIDTH / 2 + 0.8);
        }

        int topInkY(int beatIndex, boolean up) {
            int x = stemX(beatIndex, up);
            for (int y = 0; y < image.getHeight(); y++) {
                if (hasInkNear(x, y, 1)) {
                    return y;
                }
            }
            return -1;
        }

        boolean hasInkNear(int x, int y, int radius) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (isInside(x + dx, y + dy) && isNoteInk(x + dx, y + dy)) {
                        return true;
                    }
                }
            }
            return false;
        }

        boolean hasContinuousInk(int x, int fromY, int toY) {
            int top = Math.min(fromY, toY);
            int bottom = Math.max(fromY, toY);
            for (int y = top; y <= bottom; y++) {
                if (!hasInkNear(x, y, 1)) {
                    return false;
                }
            }
            return true;
        }

        private boolean isNoteInk(int x, int y) {
            return image.getRGB(x, y) == ScoreColors.INK.getRGB();
        }

        private boolean isInside(int x, int y) {
            return x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight();
        }
    }
}
