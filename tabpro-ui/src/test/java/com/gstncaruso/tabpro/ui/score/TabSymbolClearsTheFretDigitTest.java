package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TabSymbolClearsTheFretDigitTest {

    private static final int WIDTH = 300;
    private static final int BASS_TRACK = 1;

    @ParameterizedTest
    @EnumSource(value = Ornament.class, names = {"PALM_MUTE", "LET_RING"})
    void theFreeTextLabelOnTheFirstStringDoesNotBleedIntoItsOwnFretDigit(Ornament ornament) {
        Painted withLabel = paint(new Note(1, 3).toggling(ornament));
        Painted plain = paint(new Note(1, 3));

        Rectangle gapArea = new Rectangle(
                plain.layout.measureX(0), plain.layout.staffBottom(BASS_TRACK, 0),
                plain.layout.measureWidth(0), ScoreLayout.STAFF_TO_TAB_GAP);
        Rectangle digitArea = digitArea(plain.layout);

        assertTrue(withLabel.inkIn(gapArea) > plain.inkIn(gapArea),
                ornament + " tiene que dibujarse en la brecha");
        assertEquals(plain.inkIn(digitArea), withLabel.inkIn(digitArea),
                ornament + " no puede pintar tinta sobre el digito de traste de su propia cuerda");
    }

    private static Rectangle digitArea(ScoreLayout layout) {
        int tabTop = layout.tabTop(BASS_TRACK, 0);
        Rectangle beat = layout.beatBounds(BASS_TRACK, 0, 0);
        int centerX = beat.x + beat.width / 2;
        int halfHeight = ScoreLayout.STRING_SPACING / 2;
        return new Rectangle(centerX - 20, tabTop - halfHeight, 40, halfHeight * 2);
    }

    private static Measure measureWith(Note note) {
        return new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), note),
                Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter())));
    }

    private static Painted paint(Note bassNote) {
        Track guitarTrack = new Track(
                "Guitarra", Tuning.standard(), Channel.playing(25), List.of(measureWith(new Note(1, 0))));
        Track bassTrack = new Track("Bajo", Tuning.standard(), Channel.playing(33), List.of(measureWith(bassNote)));
        Score score = new Score("", 120, List.of(guitarTrack, bassTrack));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH);
        BufferedImage image = new BufferedImage(WIDTH, layout.totalHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        ScorePainter.paint(g, layout, score, new Cursor(0, 0, 0, 0), Playhead.silent());
        g.dispose();
        return new Painted(image, layout);
    }

    private record Painted(BufferedImage image, ScoreLayout layout) {
        int inkIn(Rectangle area) {
            int ink = 0;
            for (int x = area.x; x < area.x + area.width; x++) {
                for (int y = area.y; y < area.y + area.height; y++) {
                    if (image.getRGB(x, y) != ScoreColors.BACKGROUND.getRGB()) {
                        ink++;
                    }
                }
            }
            return ink;
        }
    }
}
