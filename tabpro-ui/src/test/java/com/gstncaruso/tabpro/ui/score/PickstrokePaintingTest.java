package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.gstncaruso.tabpro.core.model.effects.PickstrokeDirection;
import java.awt.Rectangle;
import java.util.List;
import org.junit.jupiter.api.Test;

class PickstrokePaintingTest {

    private static final int WIDTH = 900;

    @Test
    void aDownPickstrokeIsTheDownBowGlyph() {
        assertGlyphAt(PickstrokeDirection.DOWN, MusicFont.stringsDownBow());
    }

    @Test
    void anUpPickstrokeIsTheUpBowGlyph() {
        assertGlyphAt(PickstrokeDirection.UP, MusicFont.stringsUpBow());
    }

    private static void assertGlyphAt(PickstrokeDirection direction, String glyph) {
        Beat beat = Beat.of(Duration.quarter(), new Note(1, 0))
                .withEffects(BeatEffects.none().withPickstroke(direction));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        RecordingCanvas canvas = new RecordingCanvas();

        TabSymbolPainter.paintMeasure(canvas, layout, track, 0, 0);

        Rectangle bounds = layout.beatBounds(0, 0, 0);
        int centerX = bounds.x + bounds.width / 2;
        int tabTop = layout.tabTop(0, 0);
        assertTrue(canvas.writesTextInRegion(glyph, new Rectangle(centerX - 10, tabTop - 20, 20, 20)),
                "el pickstroke tiene que escribir el glifo de Bravura que corresponde a la direccion de la pua");
    }
}
