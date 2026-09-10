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

/**
 * Guitar Pro 5 dibuja el staccato y el acento con el glifo grabado de Bravura que corresponde al
 * lado de la nota donde caen, en vez de una elipse o un chevron trazados a mano.
 */
class ArticulationPaintingTest {

    private static final int WIDTH = 900;
    /** string=3 fret=0: grado 2, debajo de la linea del medio -la marca cae arriba de la nota. */
    private static final Note BELOW_MIDDLE_LINE = new Note(3, 0);
    /** string=1 fret=0: grado 7, arriba de la linea del medio -la marca cae abajo de la nota. */
    private static final Note ABOVE_MIDDLE_LINE = new Note(1, 0);
    /** SPACE*0.92 + SPACE*0.35, ver StaffPainter.NOTE_HEIGHT y paintArticulations: el aire entre
     * la nota y su marca de articulacion. */
    private static final double MARK_OFFSET = ScoreLayout.STAFF_LINE_SPACING * (0.92 + 0.35);

    /**
     * Debajo de la linea del medio la plica va hacia arriba (convencion estandar); el staccato
     * cae del lado de la cabeza opuesto a la plica, es decir abajo.
     */
    @Test
    void aStaccatoNoteBelowTheMiddleLineGetsTheStaccatoBelowGlyph() {
        assertGlyphNearNote(BELOW_MIDDLE_LINE, Ornament.STACCATO, MusicFont.articStaccatoBelow(), false);
    }

    /** Arriba de la linea del medio la plica va hacia abajo; el staccato cae arriba, opuesto a ella. */
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
        LienzoDePrueba lienzo = new LienzoDePrueba();

        StaffPainter.paintMeasure(lienzo, layout, track, Clef.TREBLE, 0, 0, Optional.empty());

        int step = StaffPainter.positionOf(track, Clef.TREBLE, marked, 0).step();
        int noteY = layout.stepY(0, 0, step);
        int markY = (int) Math.round(above ? noteY - MARK_OFFSET : noteY + MARK_OFFSET);
        assertTrue(lienzo.escribeTextoEnRegion(glyph, new Rectangle(0, markY - 4, WIDTH, 8)),
                "la marca tiene que escribir el glifo de Bravura del lado que corresponde de la nota");
    }
}
