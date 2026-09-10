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
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.notation.Clef;
import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ArticulationPaintingTest {

    private static final int WIDTH = 900;
    private static final Note BELOW_MIDDLE_LINE = new Note(3, 0);
    private static final Note ABOVE_MIDDLE_LINE = new Note(1, 0);
    private static final double MARK_OFFSET = ScoreLayout.STAFF_LINE_SPACING * (0.92 + 0.35);

    @Test
    void aStaccatoNoteBelowTheMiddleLineGetsTheStaccatoBelowGlyph() {
        assertGlyphNearNote(BELOW_MIDDLE_LINE, Ornament.STACCATO, MusicFont.articStaccatoBelow(), false);
    }

    @Test
    void aStaccatoNoteAboveTheMiddleLineGetsTheStaccatoAboveGlyph() {
        assertGlyphNearNote(ABOVE_MIDDLE_LINE, Ornament.STACCATO, MusicFont.articStaccatoAbove(), true);
    }

    @Test
    void anAccentedNoteBelowTheMiddleLineGetsTheAccentAboveGlyph() {
        assertGlyphNearNote(BELOW_MIDDLE_LINE, Ornament.ACCENTED, MusicFont.articAccentAbove(), true);
    }

    @Test
    void anAccentedNoteAboveTheMiddleLineGetsTheAccentBelowGlyph() {
        assertGlyphNearNote(ABOVE_MIDDLE_LINE, Ornament.ACCENTED, MusicFont.articAccentBelow(), false);
    }

    private static void assertGlyphNearNote(Note note, Ornament ornament, String glyph, boolean above) {
        Note marked = note.toggling(ornament);
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), marked)));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        RecordingCanvas canvas = new RecordingCanvas();

        StaffPainter.paintMeasure(canvas, layout, track, Clef.TREBLE, 0, 0, Optional.empty());

        int step = StaffPainter.positionOf(track, Clef.TREBLE, marked, 0).step();
        int noteY = layout.stepY(0, 0, step);
        int markY = (int) Math.round(above ? noteY - MARK_OFFSET : noteY + MARK_OFFSET);
        assertTrue(canvas.writesTextInRegion(glyph, new Rectangle(0, markY - 4, WIDTH, 8)),
                "the mark has to write the Bravura glyph on the note's corresponding side");
    }
}
