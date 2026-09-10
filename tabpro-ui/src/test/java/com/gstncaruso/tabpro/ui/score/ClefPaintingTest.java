package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.notation.Clef;
import java.awt.Rectangle;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClefPaintingTest {

    private static final int WIDTH = 900;

    @Test
    void theTrebleClefIsTheBravuraGClefGlyphAnchoredOnTheGLine() {
        RecordingCanvas canvas = new RecordingCanvas();
        ScoreLayout layout = layout();

        StaffPainter.paintClef(canvas, layout, Clef.TREBLE, 0, 0);

        int gLine = layout.staffLineY(0, 0, 1);
        assertTrue(canvas.writesTextInRegion(MusicFont.trebleClef(), new Rectangle(0, gLine - 2, WIDTH, 4)),
                "la clave de sol tiene que escribir el glifo gClef apoyado en la linea de Sol");
    }

    @Test
    void theBassClefIsTheBravuraFClefGlyphAnchoredOnTheFLine() {
        RecordingCanvas canvas = new RecordingCanvas();
        ScoreLayout layout = layout();

        StaffPainter.paintClef(canvas, layout, Clef.BASS, 0, 0);

        int fLine = layout.staffLineY(0, 0, 3);
        assertTrue(canvas.writesTextInRegion(MusicFont.bassClef(), new Rectangle(0, fLine - 2, WIDTH, 4)),
                "la clave de fa tiene que escribir el glifo fClef apoyado en la linea de Fa");
    }

    private static ScoreLayout layout() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        return ScoreLayout.of(score, WIDTH, VisibleTracks.all());
    }
}
