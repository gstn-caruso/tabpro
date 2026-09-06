package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TrackTest {

    @Test
    void aStandardGuitarTrackHasStandardTuningAndOneEmptyMeasure() {
        Track track = Track.standardGuitar("Guitarra");
        assertEquals(Tuning.standard(), track.tuning());
        assertEquals(Channel.playing(25), track.channel());
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
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(first, second));
        Track result = track.withMeasureInsertedAt(1, inserted);
        assertEquals(List.of(first, inserted, second), result.measures());
    }

    @Test
    void appendsAMeasure() {
        Measure first = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 0))));
        Measure appended = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 9))));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(first));
        Track result = track.withMeasureInsertedAt(track.measures().size(), appended);
        assertEquals(List.of(first, appended), result.measures());
    }

    @Test
    void removesAMeasure() {
        Measure first = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 0))));
        Measure second = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 1))));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(first, second));
        Track result = track.withoutMeasureAt(0);
        assertEquals(List.of(second), result.measures());
    }

    @Test
    void removingTheOnlyMeasureLeavesAnEmptyOne() {
        TimeSignature threeFour = new TimeSignature(3, 4);
        Measure onlyMeasure = new Measure(threeFour, List.of(Beat.of(Duration.quarter().longer(), new Note(1, 5))));
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(onlyMeasure));
        Track result = track.withoutMeasureAt(0);
        assertEquals(1, result.measures().size());
        Measure resultMeasure = result.measure(0);
        assertEquals(threeFour, resultMeasure.timeSignature());
        assertTrue(resultMeasure.beat(0).isRest());
        assertEquals(Duration.quarter(), resultMeasure.beat(0).duration());
    }

    @Test
    void aStandardBassTrackIsTunedFourStringsBelowTheGuitar() {
        Track track = Track.standardBass("Bajo");
        assertEquals(Tuning.standardBass(), track.tuning());
        assertEquals(4, track.tuning().stringCount());
    }

    @Test
    void changesItsChannelAndItsName() {
        Track track = Track.standardGuitar("Guitarra");

        assertEquals(80, track.withChannel(track.channel().withVolume(80)).channel().volume());
        assertEquals("Ritmica", track.withName("Ritmica").name());
        assertEquals(track.measures(), track.withName("Ritmica").measures());
    }

    @Test
    void knowsWhichOfItsMeasuresCarryNotes() {
        Measure sounding = new Measure(TimeSignature.fourFour(), List.of(Beat.of(Duration.quarter(), new Note(1, 3))));
        Measure silent = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(sounding, silent));

        assertTrue(track.hasNotesIn(0));
        assertFalse(track.hasNotesIn(1));
    }

    @Test
    void hasNoNotesBeyondItsLastMeasure() {
        Track track = Track.standardGuitar("Guitarra");

        assertFalse(track.hasNotesIn(7));
        assertFalse(track.hasNotesIn(-1));
    }

    @Test
    void countsItsMeasures() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Track track = new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of(measure, measure));

        assertEquals(2, track.measureCount());
    }

    @Test
    void rejectsATrackWithoutMeasures() {
        assertThrows(IllegalArgumentException.class,
                () -> new Track("Guitarra", Tuning.standard(), Channel.playing(25), List.of()));
    }

    @Test
    void aGuitarTrackAcceptsFretZero() {
        assertTrue(Track.standardGuitar("Guitarra").acceptsTypedNumber(0));
    }

    @Test
    void aGuitarTrackAcceptsTheHighestFret() {
        assertTrue(Track.standardGuitar("Guitarra").acceptsTypedNumber(Tuning.MAX_FRET));
    }

    @Test
    void aGuitarTrackRejectsAFretPastTheHighest() {
        assertFalse(Track.standardGuitar("Guitarra").acceptsTypedNumber(Tuning.MAX_FRET + 1));
    }

    @Test
    void aPercussionTrackRejectsASoundBelowTheLowest() {
        assertFalse(Track.percussion("Bateria").acceptsTypedNumber(PercussionKit.LOWEST_SOUND - 1));
    }

    @Test
    void aPercussionTrackAcceptsTheLowestSound() {
        assertTrue(Track.percussion("Bateria").acceptsTypedNumber(PercussionKit.LOWEST_SOUND));
    }

    @Test
    void aPercussionTrackAcceptsTheHighestSound() {
        assertTrue(Track.percussion("Bateria").acceptsTypedNumber(PercussionKit.HIGHEST_SOUND));
    }

    @Test
    void aPercussionTrackRejectsASoundPastTheHighest() {
        assertFalse(Track.percussion("Bateria").acceptsTypedNumber(PercussionKit.HIGHEST_SOUND + 1));
    }
}
