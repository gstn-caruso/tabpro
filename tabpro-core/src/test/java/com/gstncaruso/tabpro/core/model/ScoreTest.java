package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreTest {

    @Test
    void aBlankScoreHasOneGuitarTrackAndTempo120() {
        Score score = Score.blank();
        assertEquals("", score.title());
        assertEquals(120, score.tempo());
        assertEquals(1, score.tracks().size());
        assertEquals(Track.standardGuitar("Guitarra"), score.track(0));
    }

    @Test
    void replacesATrack() {
        Score score = Score.blank();
        Track newTrack = Track.standardGuitar("Bajo");
        Score replaced = score.withTrack(0, newTrack);
        assertEquals(newTrack, replaced.track(0));
    }

    @Test
    void changesTempo() {
        Score score = Score.blank();
        Score changed = score.withTempo(140);
        assertEquals(140, changed.tempo());
    }

    @Test
    void changesTitle() {
        Score score = Score.blank();
        Score changed = score.withTitle("Mi cancion");
        assertEquals("Mi cancion", changed.title());
    }

    @Test
    void addsATrackAtTheEnd() {
        Score score = Score.blank();
        Track bass = Track.standardBass("Bajo");

        Score grown = score.withTrackAdded(bass);

        assertEquals(2, grown.trackCount());
        assertEquals(bass, grown.track(1));
    }

    @Test
    void removesATrack() {
        Score score = Score.blank().withTrackAdded(Track.standardBass("Bajo"));

        Score shrunk = score.withoutTrackAt(0);

        assertEquals(1, shrunk.trackCount());
        assertEquals("Bajo", shrunk.track(0).name());
    }

    @Test
    void refusesToRemoveTheLastTrack() {
        Score score = Score.blank();

        assertThrows(IllegalStateException.class, () -> score.withoutTrackAt(0));
    }

    @Test
    void countsAsManyMeasuresAsItsLongestTrack() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Track shortTrack = Track.standardGuitar("Guitarra");
        Track longTrack = new Track("Bajo", Tuning.standardBass(), Channel.playing(33), List.of(measure, measure, measure));

        Score score = new Score("", 120, List.of(shortTrack, longTrack));

        assertEquals(3, score.measureCount());
    }

    @Test
    void everyUnmutedTrackIsAudibleWhenNobodyPlaysSolo() {
        Score score = Score.blank().withTrackAdded(Track.standardBass("Bajo"));

        assertTrue(score.isAudible(0));
        assertTrue(score.isAudible(1));
    }

    @Test
    void aMutedTrackIsNotAudible() {
        Score score = Score.blank().withTrackAdded(Track.standardBass("Bajo"));

        Score muted = score.withTrack(0, score.track(0).withChannel(score.track(0).channel().toggledMute()));

        assertFalse(muted.isAudible(0));
        assertTrue(muted.isAudible(1));
    }

    @Test
    void onlySoloTracksAreAudibleWhenSomebodyPlaysSolo() {
        Score score = Score.blank().withTrackAdded(Track.standardBass("Bajo"));

        Score soloed = score.withTrack(1, score.track(1).withChannel(score.track(1).channel().toggledSolo()));

        assertFalse(soloed.isAudible(0));
        assertTrue(soloed.isAudible(1));
    }

    @Test
    void aMutedTrackStaysSilentEvenWhileItPlaysSolo() {
        Score score = Score.blank();
        Channel mutedSolo = score.track(0).channel().toggledSolo().toggledMute();

        Score confused = score.withTrack(0, score.track(0).withChannel(mutedSolo));

        assertFalse(confused.isAudible(0));
    }

    @Test
    void insertsAMeasureInEveryTrackSoTheyStayAligned() {
        Score score = Score.blank().withTrackAdded(Track.standardBass("Bajo"));

        Score grown = score.withMeasureInsertedInEveryTrackAt(0);

        assertEquals(2, grown.track(0).measureCount());
        assertEquals(2, grown.track(1).measureCount());
        assertEquals(2, grown.measureCount());
    }

    @Test
    void aMeasureInsertedInEveryTrackKeepsTheTimeSignatureOfEachOne() {
        TimeSignature threeFour = new TimeSignature(3, 4);
        Track waltz = Track.standardBass("Bajo").withMeasure(0, Measure.empty(threeFour, Duration.quarter()));
        Score score = new Score("", 120, List.of(Track.standardGuitar("Guitarra"), waltz));

        Score grown = score.withMeasureInsertedInEveryTrackAt(0);

        assertEquals(TimeSignature.fourFour(), grown.track(0).measure(0).timeSignature());
        assertEquals(threeFour, grown.track(1).measure(0).timeSignature());
    }

    @Test
    void appendsAMeasureToEveryTrackWhenInsertingPastTheEnd() {
        Score score = Score.blank().withTrackAdded(Track.standardBass("Bajo"));

        Score grown = score.withMeasureInsertedInEveryTrackAt(score.measureCount());

        assertEquals(2, grown.track(0).measureCount());
        assertEquals(2, grown.track(1).measureCount());
    }

    @Test
    void removesAMeasureFromEveryTrack() {
        Score score = Score.blank()
                .withTrackAdded(Track.standardBass("Bajo"))
                .withMeasureInsertedInEveryTrackAt(0);

        Score shrunk = score.withoutMeasureInEveryTrackAt(0);

        assertEquals(1, shrunk.track(0).measureCount());
        assertEquals(1, shrunk.track(1).measureCount());
    }

    @Test
    void leavesShorterTracksAloneWhenRemovingAMeasureTheyDoNotHave() {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        Track longTrack = new Track("Larga", Tuning.standard(), Channel.playing(25), List.of(measure, measure));
        Score score = new Score("", 120, List.of(longTrack, Track.standardBass("Corta")));

        Score shrunk = score.withoutMeasureInEveryTrackAt(1);

        assertEquals(1, shrunk.track(0).measureCount());
        assertEquals(1, shrunk.track(1).measureCount());
    }

    @Test
    void rejectsANonPositiveTempo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Score("", 0, List.of(Track.standardGuitar("Guitarra"))));
    }

    @Test
    void rejectsAScoreWithoutTracks() {
        assertThrows(IllegalArgumentException.class, () -> new Score("", 120, List.of()));
    }
}
