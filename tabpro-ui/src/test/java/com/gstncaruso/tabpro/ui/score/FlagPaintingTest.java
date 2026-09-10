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
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.StemOverride;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FlagPaintingTest {

    private static final int WIDTH = 900;
    private static final double STEM_LENGTH = ScoreLayout.STAFF_LINE_SPACING * 3.4;
    private static final Note NOTE = new Note(2, 0);

    @Test
    void anEighthNoteWithAnUpwardsStemGetsTheEighthUpFlagAtItsTip() {
        assertFlagAt(NoteValue.EIGHTH, StemOverride.UP, MusicFont.flag8thUp());
    }

    @Test
    void anEighthNoteWithADownwardsStemGetsTheEighthDownFlagAtItsTip() {
        assertFlagAt(NoteValue.EIGHTH, StemOverride.DOWN, MusicFont.flag8thDown());
    }

    @Test
    void aSixteenthNoteWithAnUpwardsStemGetsTheSixteenthUpFlagAtItsTip() {
        assertFlagAt(NoteValue.SIXTEENTH, StemOverride.UP, MusicFont.flag16thUp());
    }

    @Test
    void aThirtySecondNoteWithAnUpwardsStemGetsTheThirtySecondUpFlagAtItsTip() {
        assertFlagAt(NoteValue.THIRTY_SECOND, StemOverride.UP, MusicFont.flag32ndUp());
    }

    @Test
    void aSixtyFourthNoteWithAnUpwardsStemGetsTheSixtyFourthUpFlagAtItsTip() {
        assertFlagAt(NoteValue.SIXTY_FOURTH, StemOverride.UP, MusicFont.flag64thUp());
    }

    private static void assertFlagAt(NoteValue value, StemOverride override, String glyph) {
        Beat beat = Beat.of(new Duration(value, false), NOTE).withEffects(BeatEffects.none().withStemOverride(override));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(beat));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        LienzoDePrueba lienzo = new LienzoDePrueba();

        StaffPainter.paintMeasure(lienzo, layout, track, Clef.TREBLE, 0, 0, Optional.empty());

        int step = StaffPosition.of(Tuning.standard().pitchOf(NOTE), Clef.TREBLE).step();
        int rootY = layout.stepY(0, 0, step);
        boolean up = override == StemOverride.UP;
        int tipY = up ? (int) Math.round(rootY - STEM_LENGTH) : (int) Math.round(rootY + STEM_LENGTH);

        assertTrue(lienzo.escribeTextoEnRegion(glyph, new Rectangle(0, tipY - 3, WIDTH, 6)),
                "la bandera tiene que escribir el glifo de Bravura en la punta de la plica");
    }
}
