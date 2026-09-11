package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.Rectangle;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PercussionNoteheadPaintingTest {

    private static final int WIDTH = 900;
    private static final int HI_HAT_CLOSED = 42;

    private static final int TAMBOURINE = 54;

    @Test
    void aCymbalSoundGetsTheXBlackNoteheadGlyph() {
        assertGlyphAt(HI_HAT_CLOSED, MusicFont.noteheadXBlack());
    }

    @Test
    void aTambourineSoundGetsTheDiamondBlackNoteheadGlyph() {
        assertGlyphAt(TAMBOURINE, MusicFont.noteheadDiamondBlack());
    }

    private static final int ACOUSTIC_SNARE = 38;

    @Test
    void anythingElseGetsTheOrdinaryBlackNoteheadGlyph() {
        assertGlyphAt(ACOUSTIC_SNARE, MusicFont.noteheadBlack());
    }

    @ParameterizedTest
    @ValueSource(ints = {42, 44, 46, 49, 51, 52, 53, 57, 59, 71, 72})
    void everyHiHatCrashRideChineseCymbalRideBellAndWhistleGetsTheXBlackNoteheadGlyph(int sound) {
        assertGlyphAt(sound, MusicFont.noteheadXBlack());
    }

    @ParameterizedTest
    @ValueSource(ints = {54, 56, 58, 67, 68, 69, 70, 73, 74, 75, 78, 79, 80, 81})
    void everyTambourineCowbellVibraslapAgogoCabasaMaracasGuiroClavesCuicaAndTriangleGetsTheDiamondBlackNoteheadGlyph(
            int sound) {
        assertGlyphAt(sound, MusicFont.noteheadDiamondBlack());
    }

    @ParameterizedTest
    @ValueSource(ints = {35, 36, 37, 38, 39, 40, 41, 43, 45, 47, 48, 50, 55, 60, 61, 62, 63, 64, 65, 66, 76, 77})
    void everyOtherGeneralMidiSoundGetsTheOrdinaryBlackNoteheadGlyph(int sound) {
        assertGlyphAt(sound, MusicFont.noteheadBlack());
    }

    private static void assertGlyphAt(int sound, String glyph) {
        Note note = new Note(1, sound);
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), note)));
        Track track = new Track("Bateria", PercussionKit.tuning(), Channel.percussion(), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        RecordingCanvas canvas = new RecordingCanvas();

        PercussionPainter.paintMeasure(canvas, layout, track, 0, 0);

        Rectangle bounds = layout.beatBounds(0, 0, 0);
        int centerX = bounds.x + bounds.width / 2;
        int y = layout.stringY(0, 0, note.string());
        assertTrue(canvas.writesTextInRegion(glyph, new Rectangle(centerX - 8, y - 8, 16, 16)),
                "the percussion notehead has to write the Bravura glyph that matches its shape");
    }
}
