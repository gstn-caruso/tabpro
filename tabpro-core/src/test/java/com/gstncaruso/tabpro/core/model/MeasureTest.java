package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MeasureTest {

    @Test
    void anEmptyMeasureHasOneRestOfTheGivenDuration() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        assertEquals(1, measure.beats().size());
        assertTrue(measure.beat(0).isRest());
        assertEquals(Duration.quarter(), measure.beat(0).duration());
    }

    @Test
    void isCompleteWhenBeatsFillTheTimeSignature() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter())));
        assertTrue(measure.isComplete());
    }

    @Test
    void isIncompleteWhenBeatsFallShort() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        assertFalse(measure.isComplete());
    }

    @Test
    void isIncompleteWhenBeatsExceed() {
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(
                Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter()),
                Beat.rest(Duration.quarter())));
        assertFalse(measure.isComplete());
    }

    @Test
    void replacesABeatAtAnIndex() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Measure replaced = measure.withBeat(0, Beat.rest(Duration.quarter().longer()));
        assertEquals(Duration.quarter().longer(), replaced.beat(0).duration());
    }

    @Test
    void insertsABeatBeforeAnIndex() {
        Beat first = Beat.of(Duration.quarter(), new Note(1, 0));
        Beat second = Beat.of(Duration.quarter(), new Note(1, 1));
        Beat inserted = Beat.of(Duration.quarter(), new Note(1, 9));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(first, second));
        Measure result = measure.withBeatInsertedAt(1, inserted);
        assertEquals(List.of(first, inserted, second), result.beats());
    }

    @Test
    void insertsABeatAtTheEnd() {
        Beat first = Beat.of(Duration.quarter(), new Note(1, 0));
        Beat appended = Beat.of(Duration.quarter(), new Note(1, 9));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(first));
        Measure result = measure.withBeatInsertedAt(measure.beats().size(), appended);
        assertEquals(List.of(first, appended), result.beats());
    }

    @Test
    void removesABeatAtAnIndex() {
        Beat first = Beat.of(Duration.quarter(), new Note(1, 0));
        Beat second = Beat.of(Duration.quarter(), new Note(1, 1));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(first, second));
        Measure result = measure.withoutBeatAt(0);
        assertEquals(List.of(second), result.beats());
    }

    @Test
    void removingTheOnlyBeatLeavesARest() {
        Beat onlyBeat = Beat.of(Duration.quarter(), new Note(1, 5));
        Measure measure = new Measure(TimeSignature.fourFour(), List.of(onlyBeat));
        Measure result = measure.withoutBeatAt(0);
        assertTrue(result.beat(0).isRest());
        assertEquals(Duration.quarter(), result.beat(0).duration());
    }

    @Test
    void rejectsABeatIndexOutOfRange() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        assertThrows(IndexOutOfBoundsException.class, () -> measure.beat(5));
    }

    @Test
    void rejectsAnEmptyBeatList() {
        assertThrows(IllegalArgumentException.class, () -> new Measure(TimeSignature.fourFour(), List.of()));
    }
}
