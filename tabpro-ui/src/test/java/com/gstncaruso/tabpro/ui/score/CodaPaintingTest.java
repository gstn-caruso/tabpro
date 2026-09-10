package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import java.awt.Rectangle;
import java.util.List;
import org.junit.jupiter.api.Test;

class CodaPaintingTest {

    private static final int WIDTH = 900;

    @Test
    void theCodaSymbolIsTheBravuraCodaGlyphAboveTheSystem() {
        Track guitar = Track.standardGuitar("Guitarra");
        Measure bar = new Measure(TimeSignature.fourFour(), List.of(Beat.rest(Duration.quarter())));
        Score score = new Score("", 120, List.of(
                new Track("Guitarra", guitar.tuning(), guitar.channel(), List.of(bar))));
        score = score.mappingTrack(0, track -> track.mappingMeasure(0, measure ->
                measure.withAttributes(measure.attributes().withSymbol(DirectionSymbol.CODA))));

        ScoreLayout layout = ScoreLayout.of(score, WIDTH, VisibleTracks.all());
        RecordingCanvas lienzo = new RecordingCanvas();

        BarStructurePainter.paintScoreWide(lienzo, layout, score.track(0), 0, 0);

        int left = layout.measureX(0);
        int right = left + layout.measureWidth(0);
        int centerX = (left + right) / 2;
        int y = layout.staffTop(0, 0) - 16;
        assertTrue(lienzo.writesTextInRegion(MusicFont.coda(), new Rectangle(centerX - 10, y - 10, 20, 20)),
                "la coda tiene que escribir el glifo de Bravura arriba del sistema");
    }
}
