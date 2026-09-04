package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TrackTest {

    @Test
    void aStandardGuitarTrackHasStandardTuningAndOneEmptyMeasure() {
        Track track = Track.standardGuitar("Guitarra");
        assertEquals(Tuning.standard(), track.tuning());
        assertEquals(25, track.midiProgram());
        assertEquals(1, track.measures().size());
        assertTrue(track.measure(0).beat(0).isRest());
    }

    @Test
    void replacesAMeasure() {
        Track track = Track.standardGuitar("Guitarra");
        Measure newMeasure = Measure.empty(TimeSignature.fourFour(), Duration.quarter().longer());
        Track replaced = track.withMeasure(0, newMeasure);
        assertEquals(newMeasure, replaced.measure(0));
    }

    @Test
    void insertsAMeasureBefore() {
        Measure first = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 0))));
        Measure second = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 1))));
        Measure inserted = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 9))));
        Track track = new Track("Guitarra", Tuning.standard(), 25, List.of(first, second));
        Track result = track.withMeasureInsertedAt(1, inserted);
        assertEquals(List.of(first, inserted, second), result.measures());
    }

    @Test
    void appendsAMeasure() {
        Measure first = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 0))));
        Measure appended = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 9))));
        Track track = new Track("Guitarra", Tuning.standard(), 25, List.of(first));
        Track result = track.withMeasureInsertedAt(track.measures().size(), appended);
        assertEquals(List.of(first, appended), result.measures());
    }

    @Test
    void removesAMeasure() {
        Measure first = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 0))));
        Measure second = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 1))));
        Track track = new Track("Guitarra", Tuning.standard(), 25, List.of(first, second));
        Track result = track.withoutMeasureAt(0);
        assertEquals(List.of(second), result.measures());
    }

    @Test
    void removingTheOnlyMeasureLeavesAnEmptyOne() {
        TimeSignature threeFour = new TimeSignature(3, 4);
        Measure onlyMeasure = new Measure(threeFour, List.of(Beat.of(Duration.quarter().longer(), new Note(1, 5))));
        Track track = new Track("Guitarra", Tuning.standard(), 25, List.of(onlyMeasure));
        Track result = track.withoutMeasureAt(0);
        assertEquals(1, result.measures().size());
        Measure resultMeasure = result.measure(0);
        assertEquals(threeFour, resultMeasure.timeSignature());
        assertTrue(resultMeasure.beat(0).isRest());
        assertEquals(Duration.quarter(), resultMeasure.beat(0).duration());
    }

    @Test
    void rejectsAMidiProgramBelowZero() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        assertThrows(IllegalArgumentException.class,
                () -> new Track("Guitarra", Tuning.standard(), -1, List.of(measure)));
    }

    @Test
    void rejectsAMidiProgramAbove127() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        assertThrows(IllegalArgumentException.class,
                () -> new Track("Guitarra", Tuning.standard(), 128, List.of(measure)));
    }

    @Test
    void rejectsATrackWithoutMeasures() {
        assertThrows(IllegalArgumentException.class,
                () -> new Track("Guitarra", Tuning.standard(), 25, List.of()));
    }
}
