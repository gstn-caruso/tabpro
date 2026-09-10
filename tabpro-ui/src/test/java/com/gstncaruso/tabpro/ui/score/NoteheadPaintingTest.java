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

class NoteheadPaintingTest {

    private static final int WIDTH = 900;

    @Test
    void aQuarterNoteHeadIsTheBlackNoteheadGlyph() {
        assertGlyphAtNoteLine(NoteValue.QUARTER, MusicFont.noteheadBlack());
    }

    @Test
    void aHalfNoteHeadIsTheHalfNoteheadGlyph() {
        assertGlyphAtNoteLine(NoteValue.HALF, MusicFont.noteheadHalf());
    }

    @Test
    void aWholeNoteHeadIsTheWholeNoteheadGlyph() {
        assertGlyphAtNoteLine(NoteValue.WHOLE, MusicFont.noteheadWhole());
    }

    private static void assertGlyphAtNoteLine(NoteValue value, String glyph) {
        Note note = new Note(3, 5);
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(Beat.of(new Duration(value, false), note)));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        RecordingCanvas canvas = new RecordingCanvas();

        StaffPainter.paintMeasure(canvas, layout, track, Clef.TREBLE, 0, 0, Optional.empty());

        int step = StaffPainter.positionOf(track, Clef.TREBLE, note, 0).step();
        int y = layout.stepY(0, 0, step);
        assertTrue(canvas.writesTextInRegion(glyph, new Rectangle(0, y - 2, WIDTH, 4)),
                "la cabeza tiene que escribir el glifo de Bravura sobre la linea de la nota");
    }
}
