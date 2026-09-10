package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.awt.Rectangle;
import java.util.List;
import org.junit.jupiter.api.Test;

class TimeSignaturePaintingTest {

    private static final int WIDTH = 900;

    @Test
    void writesTheBeatsGlyphAboveTheBeatUnitGlyphOnTheMiddleStaffLines() {
        ScoreLayout layout = layoutFor(new TimeSignature(4, 4));
        LienzoDePrueba lienzo = paint(new TimeSignature(4, 4), layout);

        String four = MusicFont.timeSignatureDigit(4);
        assertTrue(lienzo.escribeTextoEnRegion(four, new Rectangle(0, layout.staffLineY(0, 0, 3) - 2, WIDTH, 4)),
                "el numerador tiene que escribir el digito de Bravura sobre la linea del medio de arriba");
        assertTrue(lienzo.escribeTextoEnRegion(four, new Rectangle(0, layout.staffLineY(0, 0, 1) - 2, WIDTH, 4)),
                "el denominador tiene que escribir el digito de Bravura sobre la linea del medio de abajo");
    }

    @Test
    void aTwoDigitBeatCountIsAssembledGlyphByGlyph() {
        ScoreLayout layout = layoutFor(new TimeSignature(12, 8));
        LienzoDePrueba lienzo = paint(new TimeSignature(12, 8), layout);

        String twelve = MusicFont.timeSignatureDigit(1) + MusicFont.timeSignatureDigit(2);
        assertTrue(lienzo.escribeTextoEnRegion(twelve, new Rectangle(0, layout.staffLineY(0, 0, 3) - 2, WIDTH, 4)),
                "12 se arma con el digito 1 y el digito 2 de Bravura, uno detras del otro");
    }

    private static LienzoDePrueba paint(TimeSignature signature, ScoreLayout layout) {
        LienzoDePrueba lienzo = new LienzoDePrueba();
        Track track = trackWith(signature);
        StaffPainter.paintTimeSignature(lienzo, layout, track, 0, 0, layout.measureX(0) + 4);
        return lienzo;
    }

    private static ScoreLayout layoutFor(TimeSignature signature) {
        Score score = new Score("", 120, List.of(trackWith(signature)));
        return ScoreLayout.of(score, WIDTH, VisibleTracks.all());
    }

    private static Track trackWith(TimeSignature signature) {
        Measure measure = Measure.empty(signature, Duration.quarter());
        return new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure));
    }
}
