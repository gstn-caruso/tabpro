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
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import com.gstncaruso.tabpro.core.notation.Clef;
import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AccidentalPaintingTest {

    private static final int WIDTH = 900;

    @Test
    void aNoteOutsideTheKeyIsMarkedWithTheSharpGlyph() {
        Note note = new Note(3, 3);
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), note)));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        RecordingCanvas canvas = new RecordingCanvas();

        StaffPainter.paintMeasure(canvas, layout, track, Clef.TREBLE, 0, 0, Optional.empty());

        int step = StaffPainter.positionOf(track, Clef.TREBLE, note, 0).step();
        int y = layout.stepY(0, 0, step);
        assertTrue(canvas.writesTextInRegion(MusicFont.accidentalSharp(), new Rectangle(0, y - 2, WIDTH, 4)),
                "la nota fuera de la armadura tiene que llevar el glifo del sostenido");
    }

    @Test
    void aSharpKeySignatureIsWrittenWithTheSharpGlyph() {
        ScoreLayout layout = layoutFor(new KeySignature(1, Mode.MAJOR));
        RecordingCanvas canvas = new RecordingCanvas();

        StaffPainter.paintKeySignature(
                canvas, layout, Clef.TREBLE, new KeySignature(1, Mode.MAJOR), 0, 0, layout.measureX(0) + 4);

        int y = layout.stepY(0, 0, 8);
        assertTrue(canvas.writesTextInRegion(MusicFont.accidentalSharp(), new Rectangle(0, y - 2, WIDTH, 4)),
                "el fa sostenido de la armadura tiene que llevar el glifo del sostenido");
    }

    @Test
    void aFlatKeySignatureIsWrittenWithTheFlatGlyph() {
        ScoreLayout layout = layoutFor(new KeySignature(-1, Mode.MAJOR));
        RecordingCanvas canvas = new RecordingCanvas();

        StaffPainter.paintKeySignature(
                canvas, layout, Clef.TREBLE, new KeySignature(-1, Mode.MAJOR), 0, 0, layout.measureX(0) + 4);

        int y = layout.stepY(0, 0, 4);
        assertTrue(canvas.writesTextInRegion(MusicFont.accidentalFlat(), new Rectangle(0, y - 2, WIDTH, 4)),
                "el si bemol de la armadura tiene que llevar el glifo del bemol");
    }

    @Test
    void aNoteReturningToTheKeyAfterASharpIsMarkedWithTheNaturalGlyph() {
        Note sharped = new Note(3, 3);
        Note backToNatural = new Note(3, 2);
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.of(Duration.quarter(), sharped), Beat.of(Duration.quarter(), backToNatural)));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        RecordingCanvas canvas = new RecordingCanvas();

        StaffPainter.paintMeasure(canvas, layout, track, Clef.TREBLE, 0, 0, Optional.empty());

        int step = StaffPainter.positionOf(track, Clef.TREBLE, backToNatural, 0).step();
        int y = layout.stepY(0, 0, step);
        assertTrue(canvas.writesTextInRegion(MusicFont.accidentalNatural(), new Rectangle(0, y - 2, WIDTH, 4)),
                "la nota que vuelve a la armadura tiene que llevar el glifo del becuadro");
    }

    private static ScoreLayout layoutFor(KeySignature key) {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        return ScoreLayout.of(score, WIDTH, VisibleTracks.all());
    }
}
