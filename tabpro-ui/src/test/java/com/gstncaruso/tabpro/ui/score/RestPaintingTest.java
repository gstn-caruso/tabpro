package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.notation.Clef;
import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Guitar Pro 5 dibuja cada silencio con el glifo grabado de Bravura que corresponde a su figura
 * en vez de un rectangulo, un path o un gancho trazados a mano.
 */
class RestPaintingTest {

    private static final int WIDTH = 900;

    @Test
    void aWholeRestIsTheWholeRestGlyphHangingFromTheFourthLine() {
        assertGlyphAt(NoteValue.WHOLE, MusicFont.restWhole(), 6);
    }

    @Test
    void aHalfRestIsTheHalfRestGlyphSittingOnTheMiddleLine() {
        assertGlyphAt(NoteValue.HALF, MusicFont.restHalf(), 4);
    }

    @Test
    void aQuarterRestIsTheQuarterRestGlyphCenteredOnTheStaff() {
        assertGlyphAt(NoteValue.QUARTER, MusicFont.restQuarter(), 4);
    }

    @Test
    void anEighthRestIsTheEighthRestGlyphCenteredOnTheStaff() {
        assertGlyphAt(NoteValue.EIGHTH, MusicFont.rest8th(), 4);
    }

    @Test
    void aSixteenthRestIsTheSixteenthRestGlyphCenteredOnTheStaff() {
        assertGlyphAt(NoteValue.SIXTEENTH, MusicFont.rest16th(), 4);
    }

    @Test
    void aThirtySecondRestIsTheThirtySecondRestGlyphCenteredOnTheStaff() {
        assertGlyphAt(NoteValue.THIRTY_SECOND, MusicFont.rest32nd(), 4);
    }

    private static void assertGlyphAt(NoteValue value, String glyph, int step) {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(Beat.rest(new Duration(value, false))));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        LienzoDePrueba lienzo = new LienzoDePrueba();

        StaffPainter.paintMeasure(lienzo, layout, track, Clef.TREBLE, 0, 0, Optional.empty());

        int y = layout.stepY(0, 0, step);
        assertTrue(lienzo.escribeTextoEnRegion(glyph, new Rectangle(0, y - 2, WIDTH, 4)),
                "el silencio tiene que escribir el glifo de Bravura en su linea de referencia");
    }
}
