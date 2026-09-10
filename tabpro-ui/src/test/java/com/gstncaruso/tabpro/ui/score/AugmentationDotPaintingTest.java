package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AugmentationDotPaintingTest {

    private static final int WIDTH = 900;

    @Test
    void aDottedNoteGetsTheAugmentationDotGlyph() {
        Note note = new Note(3, 5);
        Measure measure = new Measure(
                TimeSignature.fourFour(), List.of(Beat.of(new Duration(NoteValue.QUARTER, true), note)));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        RecordingCanvas lienzo = new RecordingCanvas();

        StaffPainter.paintMeasure(lienzo, layout, track, Clef.TREBLE, 0, 0, Optional.empty());

        int step = StaffPainter.positionOf(track, Clef.TREBLE, note, 0).step();
        int y = layout.stepY(0, 0, step);
        assertTrue(lienzo.writesTextInRegion(MusicFont.augmentationDot(), new Rectangle(0, y - 2, WIDTH, 4)),
                "la nota con puntillo tiene que llevar el glifo del puntillo junto a su cabeza");
    }

    @Test
    void aDottedRestGetsTheAugmentationDotGlyph() {
        Measure measure = new Measure(
                TimeSignature.fourFour(), List.of(Beat.rest(new Duration(NoteValue.HALF, true))));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        RecordingCanvas lienzo = new RecordingCanvas();

        StaffPainter.paintMeasure(lienzo, layout, track, Clef.TREBLE, 0, 0, Optional.empty());

        int y = layout.stepY(0, 0, 5);
        assertTrue(lienzo.writesTextInRegion(MusicFont.augmentationDot(), new Rectangle(0, y - 2, WIDTH, 4)),
                "el silencio con puntillo tiene que llevar el glifo del puntillo");
    }
}
