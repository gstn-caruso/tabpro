package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * El triplet feel toca las corcheas (o semicorcheas) con swing sin cambiar
 * lo escrito: la primera del par se estira, la segunda se acorta.
 */
class SwingTimingTest {

    private static final Duration EIGHTH = new Duration(NoteValue.EIGHTH, false);
    private static final Duration QUARTER = Duration.quarter();

    @Test
    void sinTripletFeelLasDuracionesQuedanComoEstan() {
        List<Beat> beats = List.of(Beat.of(EIGHTH, new Note(1, 0)), Beat.of(EIGHTH, new Note(1, 1)));

        long[] durations = SwingTiming.durationsFor(beats, TripletFeel.NONE);

        assertArrayEquals(new long[] {EIGHTH.ticks(), EIGHTH.ticks()}, durations);
    }

    @Test
    void unParDeCorcheasSeReparteDosTerciosYUnTercio() {
        List<Beat> beats = List.of(Beat.of(EIGHTH, new Note(1, 0)), Beat.of(EIGHTH, new Note(1, 1)));

        long[] durations = SwingTiming.durationsFor(beats, TripletFeel.EIGHTH);

        long pair = EIGHTH.ticks() * 2;
        assertArrayEquals(new long[] {pair * 2 / 3, pair - pair * 2 / 3}, durations);
    }

    @Test
    void unaFiguraQueNoEsElValorDelSwingQuedaIntacta() {
        List<Beat> beats = List.of(Beat.of(QUARTER, new Note(1, 0)), Beat.of(EIGHTH, new Note(1, 1)));

        long[] durations = SwingTiming.durationsFor(beats, TripletFeel.EIGHTH);

        assertArrayEquals(new long[] {QUARTER.ticks(), EIGHTH.ticks()}, durations);
    }

    @Test
    void unaCorcheaSueltaSinParejaQuedaIgual() {
        List<Beat> beats = List.of(Beat.of(EIGHTH, new Note(1, 0)), Beat.of(QUARTER, new Note(1, 1)));

        long[] durations = SwingTiming.durationsFor(beats, TripletFeel.EIGHTH);

        assertArrayEquals(new long[] {EIGHTH.ticks(), QUARTER.ticks()}, durations);
    }

    @Test
    void dosParesConsecutivosSeSwinguanCadaUnoPorSeparado() {
        List<Beat> beats = List.of(
                Beat.of(EIGHTH, new Note(1, 0)), Beat.of(EIGHTH, new Note(1, 1)),
                Beat.of(EIGHTH, new Note(1, 2)), Beat.of(EIGHTH, new Note(1, 3)));

        long[] durations = SwingTiming.durationsFor(beats, TripletFeel.EIGHTH);

        long pair = EIGHTH.ticks() * 2;
        long first = pair * 2 / 3;
        long second = pair - first;
        assertArrayEquals(new long[] {first, second, first, second}, durations);
    }
}
