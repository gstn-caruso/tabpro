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

/**
 * Guitar Pro 5 graba la clave con el glifo grabado de Bravura -trazo modulado, espiral cerrada-
 * en vez de dibujarla a mano con un Path2D de grosor uniforme.
 */
class ClefPaintingTest {

    private static final int WIDTH = 900;

    @Test
    void theTrebleClefIsTheBravuraGClefGlyphAnchoredOnTheGLine() {
        LienzoDePrueba lienzo = new LienzoDePrueba();
        ScoreLayout layout = layout();

        StaffPainter.paintClef(lienzo, layout, Clef.TREBLE, 0, 0);

        int gLine = layout.staffLineY(0, 0, 1);
        assertTrue(lienzo.escribeTextoEnRegion(MusicFont.trebleClef(), new Rectangle(0, gLine - 2, WIDTH, 4)),
                "la clave de sol tiene que escribir el glifo gClef apoyado en la linea de Sol");
    }

    private static ScoreLayout layout() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
        Score score = new Score("", 120, List.of(track));
        return ScoreLayout.of(score, WIDTH, VisibleTracks.all());
    }
}
