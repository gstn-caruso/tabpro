package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * "Ver > Notas con dinamica [F11]" del manual: en vez de la tinta pareja de siempre, la cabeza
 * de la nota se dibuja con un gradiente de color -mas clara cuanto mas suave, mas oscura cuanto
 * mas fuerte- sin que eso cambie donde se escribe la nota ni como suena.
 */
class StaffPainterTest {

    private static final int WIDTH = 900;

    @Test
    void aSoftAndALoudNoteGetDifferentInkWhenDynamicNotesAreShown() {
        Painted painted = paint(Dynamic.PIANO_PIANISSIMO, Dynamic.FORTE_FORTISSIMO, true);

        assertNotEquals(
                painted.image().getRGB(painted.noteX(0), painted.noteY()),
                painted.image().getRGB(painted.noteX(1), painted.noteY()),
                "una nota suave y una fuerte tienen que pintarse distinto");
    }

    @Test
    void aLouderNoteReadsCloserToTheFullInkThanASofterOneOnTheDarkScreen() {
        Painted painted = paint(Dynamic.PIANO_PIANISSIMO, Dynamic.FORTE_FORTISSIMO, true);

        int softRgb = painted.image().getRGB(painted.noteX(0), painted.noteY());
        int loudRgb = painted.image().getRGB(painted.noteX(1), painted.noteY());

        assertTrue(brightnessOf(loudRgb) > brightnessOf(softRgb),
                "sobre el fondo oscuro, cuanto mas fuerte suena una nota mas clara se dibuja");
    }

    @Test
    void withoutTheOptionEveryNoteKeepsThePlainInkRegardlessOfItsDynamic() {
        Painted painted = paint(Dynamic.PIANO_PIANISSIMO, Dynamic.FORTE_FORTISSIMO, false);

        assertEquals(ScoreColors.INK.getRGB(), painted.image().getRGB(painted.noteX(0), painted.noteY()));
        assertEquals(ScoreColors.INK.getRGB(), painted.image().getRGB(painted.noteX(1), painted.noteY()));
    }

    @Test
    void theLoudestDynamicPaintsExactlyTheOrdinaryInk() {
        Painted painted = paint(Dynamic.FORTE_FORTISSIMO, Dynamic.FORTE_FORTISSIMO, true);

        assertEquals(ScoreColors.INK.getRGB(), painted.image().getRGB(painted.noteX(0), painted.noteY()));
    }

    private static double brightnessOf(int rgb) {
        Color color = new Color(rgb);
        return (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
    }

    private static Painted paint(Dynamic first, Dynamic second, boolean showsDynamicNotes) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), new Note(6, 0).withDynamic(first)),
                Beat.of(Duration.quarter(), new Note(6, 0).withDynamic(second))));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));

        ScoreLayout layout = ScoreLayout.of(
                score, WIDTH, VisibleTracks.all(), VisibleNotations.both(), showsDynamicNotes);
        BufferedImage image = new BufferedImage(WIDTH, layout.totalHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setClip(0, 0, WIDTH, layout.totalHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        ScorePainter.paint(g, layout, score, new Cursor(0, 0, 0, 1), Playhead.silent());
        g.dispose();
        return new Painted(image, layout);
    }

    private record Painted(BufferedImage image, ScoreLayout layout) {

        int noteX(int beatIndex) {
            Rectangle beat = layout.beatBounds(0, 0, beatIndex);
            return beat.x + beat.width / 2;
        }

        int noteY() {
            StaffPosition position = StaffPosition.of(Tuning.standard().pitchOf(new Note(6, 0)), Clef.TREBLE);
            return layout.stepY(0, 0, position.step());
        }
    }
}
