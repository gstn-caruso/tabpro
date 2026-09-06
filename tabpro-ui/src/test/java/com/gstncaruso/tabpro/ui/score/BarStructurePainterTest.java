package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.playback.PlayOrder;
import java.util.List;
import org.junit.jupiter.api.Test;

class BarStructurePainterTest {

    /**
     * El cartelito tiene que decir las vueltas que la partitura realmente toca. El dialogo pide
     * ese numero -"cierra despues de tantas vueltas"- y PlayOrder vuelve al principio mientras
     * las vueltas cerradas sean menos que ese numero, asi que repeatCount ya es el total.
     * Sumarle uno al dibujarlo hacia que la hoja prometiera una vuelta que nunca sonaba.
     */
    @Test
    void theRepeatLabelSaysTheTimesTheScoreActuallyPlays() {
        assertEquals("x2", BarStructurePainter.repeatLabel(2));
        assertEquals("x3", BarStructurePainter.repeatLabel(3));
    }

    /** La prueba de que ese numero es el mismo que se escucha, y no otra convencion. */
    @Test
    void theRepeatLabelMatchesHowManyTimesThePlayOrderVisitsTheBar() {
        int times = 3;
        Score score = scoreThatRepeatsItsOnlyBar(times);

        long visits = PlayOrder.of(score).measureIndexes().stream().filter(index -> index == 0).count();

        assertEquals("x" + visits, BarStructurePainter.repeatLabel(times));
    }

    private static Score scoreThatRepeatsItsOnlyBar(int times) {
        Track guitar = Track.standardGuitar("Guitarra");
        Measure bar = new Measure(TimeSignature.fourFour(), List.of(Beat.rest(Duration.quarter())));
        Score score = new Score("", 120, List.of(
                new Track("Guitarra", guitar.tuning(), guitar.channel(), List.of(bar))));
        return score.mappingTrack(0, track -> track.mappingMeasure(0, measure -> measure.withAttributes(
                measure.attributes().withRepeatOpen(true).withRepeatCount(times))));
    }
}
