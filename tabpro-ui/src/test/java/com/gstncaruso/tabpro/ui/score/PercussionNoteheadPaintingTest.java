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

/**
 * Guitar Pro 5 dibuja la cabeza de percusion con el glifo grabado de Bravura que corresponde a su
 * forma -X, rombo u ovalo- en vez de dos diagonales, un Path2D o una elipse trazados a mano.
 */
class PercussionNoteheadPaintingTest {

    private static final int WIDTH = 900;
    /** 42 = "Hi-hat cerrado": shapeFor lo mapea a CROSS. */
    private static final int HI_HAT_CLOSED = 42;

    /** 54 = "Pandereta": shapeFor lo mapea a DIAMOND. */
    private static final int TAMBOURINE = 54;

    @Test
    void aCymbalSoundGetsTheXBlackNoteheadGlyph() {
        assertGlyphAt(HI_HAT_CLOSED, MusicFont.noteheadXBlack());
    }

    @Test
    void aTambourineSoundGetsTheDiamondBlackNoteheadGlyph() {
        assertGlyphAt(TAMBOURINE, MusicFont.noteheadDiamondBlack());
    }

    /** 38 = "Caja acustica": shapeFor no la reconoce ni como platillo ni como pandereta, asi que
     * cae en el ovalo comun. */
    private static final int ACOUSTIC_SNARE = 38;

    @Test
    void anythingElseGetsTheOrdinaryBlackNoteheadGlyph() {
        assertGlyphAt(ACOUSTIC_SNARE, MusicFont.noteheadBlack());
    }

    private static void assertGlyphAt(int sound, String glyph) {
        Note note = new Note(1, sound);
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), note)));
        Track track = new Track("Bateria", PercussionKit.tuning(), Channel.percussion(), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        LienzoDePrueba lienzo = new LienzoDePrueba();

        PercussionPainter.paintMeasure(lienzo, layout, track, 0, 0);

        Rectangle bounds = layout.beatBounds(0, 0, 0);
        int centerX = bounds.x + bounds.width / 2;
        int y = layout.stringY(0, 0, note.string());
        assertTrue(lienzo.escribeTextoEnRegion(glyph, new Rectangle(centerX - 8, y - 8, 16, 16)),
                "la cabeza de percusion tiene que escribir el glifo de Bravura que corresponde a su forma");
    }
}
