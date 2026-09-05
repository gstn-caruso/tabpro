package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Score;
import java.util.List;
import org.junit.jupiter.api.Test;

/** El metronomo: su propio sonido, activable, marcando cada pulso del compas. */
class MetronomeTest {

    @Test
    void apagadoNoProduceClicks() {
        Score score = Score.blank();

        List<MetronomeClick> clicks = Metronome.off().clicksFor(score);

        assertTrue(clicks.isEmpty());
    }

    @Test
    void marcaUnClickPorPulsoDelCompas() {
        Score score = Score.blank(); // 4/4 por defecto

        List<MetronomeClick> clicks = Metronome.on().clicksFor(score);

        assertEquals(4, clicks.size());
    }

    @Test
    void elPrimerPulsoDeCadaCompasEsAcentuado() {
        Score score = Score.blank();

        List<MetronomeClick> clicks = Metronome.on().clicksFor(score);

        assertTrue(clicks.get(0).accented());
        assertTrue(clicks.subList(1, clicks.size()).stream().noneMatch(MetronomeClick::accented));
    }

    @Test
    void losPulsosEstanEspaciadosPorCorchera() {
        Score score = Score.blank();

        List<MetronomeClick> clicks = Metronome.on().clicksFor(score);

        long quarter = com.gstncaruso.tabpro.core.model.Duration.quarter().ticks();
        assertEquals(List.of(0L, quarter, quarter * 2, quarter * 3),
                clicks.stream().map(MetronomeClick::tick).toList());
    }

    @Test
    void elSonidoAcentuadoEsDistintoDelSonidoNormal() {
        MetronomeClick accented = new MetronomeClick(0, true);
        MetronomeClick plain = new MetronomeClick(0, false);

        assertTrue(accented.sound() != plain.sound());
    }

    @Test
    void sigueElOrdenDeReproduccionConRepeticiones() {
        Score score = Score.blank().withMeasureInsertedInEveryTrackAt(1);
        Score withRepeat = score.withAttributesInEveryTrackAt(1,
                com.gstncaruso.tabpro.core.model.bars.MeasureAttributes.plain()
                        .withRepeatOpen(false).withRepeatCount(2));
        // ambos compases se repiten si el primero abre y el segundo cierra
        Score fullScore = withRepeat.withAttributesInEveryTrackAt(0,
                com.gstncaruso.tabpro.core.model.bars.MeasureAttributes.plain().withRepeatOpen(true));

        List<MetronomeClick> clicks = Metronome.on().clicksFor(fullScore);

        assertEquals(16, clicks.size()); // 2 compases de 4 pulsos, dos vueltas
    }
}
