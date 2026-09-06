package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VisibleTracksTest {

    @Test
    void theMultitrackViewShowsEveryTrack() {
        VisibleTracks tracks = VisibleTracks.all();

        assertTrue(tracks.shows(0));
        assertTrue(tracks.shows(1));
        assertTrue(tracks.shows(2));
    }

    @Test
    void withoutTheMultitrackViewOnlyTheActiveTrackShows() {
        VisibleTracks tracks = VisibleTracks.all().withMultitrack(false).withActiveTrack(1);

        assertFalse(tracks.shows(0));
        assertTrue(tracks.shows(1));
        assertFalse(tracks.shows(2));
    }

    @Test
    void aTrackTurnedOffInTheMixTableDoesNotShow() {
        VisibleTracks tracks = VisibleTracks.all().withActiveTrack(0).withTrackShown(1, false);

        assertTrue(tracks.shows(0));
        assertFalse(tracks.shows(1));
    }

    @Test
    void theActiveTrackShowsEvenWhenItWasTurnedOffInTheMixTable() {
        VisibleTracks tracks = VisibleTracks.all().withActiveTrack(1).withTrackShown(1, false);

        assertTrue(tracks.shows(1));
    }

    @Test
    void turningATrackBackOnMakesItShowAgain() {
        VisibleTracks tracks = VisibleTracks.all().withTrackShown(1, false).withTrackShown(1, true);

        assertTrue(tracks.shows(1));
    }

    @Test
    void theTracksTurnedOffAreKeptWhileTheMultitrackViewIsOff() {
        VisibleTracks tracks = VisibleTracks.all()
                .withTrackShown(1, false)
                .withMultitrack(false)
                .withMultitrack(true);

        assertFalse(tracks.shows(1));
    }
}
