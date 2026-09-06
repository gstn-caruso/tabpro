package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrackVisibilityTest {

    private final TrackVisibility visibility = new TrackVisibility();

    @Test
    void everyTrackStartsTurnedOnAndInTheMultitrackView() {
        assertTrue(visibility.isMultitrack());
        assertTrue(visibility.isTurnedOn(0));
        assertTrue(visibility.isTurnedOn(7));
    }

    @Test
    void turningATrackOffIsRememberedAndReachesTheScore() {
        visibility.setTurnedOn(1, false);

        assertFalse(visibility.isTurnedOn(1));
        assertFalse(visibility.tracks().withActiveTrack(0).shows(1));
    }

    @Test
    void leavingTheMultitrackViewDoesNotForgetWhichTracksWereTurnedOff() {
        visibility.setTurnedOn(1, false);
        visibility.setMultitrack(false);
        visibility.setMultitrack(true);

        assertFalse(visibility.isTurnedOn(1));
    }

    @Test
    void whoeverIsInterestedHearsAboutEveryChange() {
        List<String> heard = new ArrayList<>();
        visibility.onChange(() -> heard.add("cambio"));

        visibility.setMultitrack(false);
        visibility.setTurnedOn(2, false);

        assertEquals(List.of("cambio", "cambio"), heard);
    }
}
