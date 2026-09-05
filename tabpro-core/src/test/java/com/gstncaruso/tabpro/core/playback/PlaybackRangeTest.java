package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gstncaruso.tabpro.core.model.Score;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Reproducir desde una posicion o solo un rango: una seleccion de compases
 * que se toca de corrido, sin repeticiones ni saltos.
 */
class PlaybackRangeTest {

    @Test
    void unRangoEsUnaSecuenciaSimpleDeCompases() {
        Score score = scoreWithMeasures(5);
        PlaybackRange range = new PlaybackRange(1, 3);

        PlayOrder order = range.asPlayOrder(score);

        assertEquals(List.of(1, 2, 3), order.measureIndexes());
    }

    @Test
    void desdeUnaPosicionLlegaHastaElFinal() {
        Score score = scoreWithMeasures(4);

        PlayOrder order = PlaybackRange.from(2, score).asPlayOrder(score);

        assertEquals(List.of(2, 3), order.measureIndexes());
    }

    @Test
    void laPartituraEnteraEsElRangoCompleto() {
        Score score = scoreWithMeasures(3);

        PlayOrder order = PlaybackRange.whole(score).asPlayOrder(score);

        assertEquals(List.of(0, 1, 2), order.measureIndexes());
    }

    @Test
    void rechazaUnRangoInvertido() {
        assertThrows(IllegalArgumentException.class, () -> new PlaybackRange(3, 1));
    }

    @Test
    void rechazaUnCompasInicialNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new PlaybackRange(-1, 2));
    }

    @Test
    void seAcotaAlUltimoCompasDeLaPartitura() {
        Score score = scoreWithMeasures(3);
        PlaybackRange range = new PlaybackRange(1, 100);

        PlayOrder order = range.asPlayOrder(score);

        assertEquals(List.of(1, 2), order.measureIndexes());
    }

    private Score scoreWithMeasures(int count) {
        Score score = Score.blank();
        for (int i = 1; i < count; i++) {
            score = score.withMeasureInsertedInEveryTrackAt(i);
        }
        return score;
    }
}
