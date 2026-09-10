package com.gstncaruso.tabpro.ui.score;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HorizontalMultitrackTest {

    private final TrackVisibility visibleTracks = new TrackVisibility();

    @Test
    void withThePreferenceOnEnteringHorizontalScreenTurnsOnTheMultitrackView() {
        visibleTracks.setMultitrack(false);

        HorizontalMultitrack.applyTo(visibleTracks, ViewMode.SCREEN_HORIZONTAL, true);

        assertTrue(visibleTracks.isMultitrack());
    }

    @Test
    void withThePreferenceOffEnteringHorizontalScreenDoesNotTouchTheMultitrackView() {
        visibleTracks.setMultitrack(false);

        HorizontalMultitrack.applyTo(visibleTracks, ViewMode.SCREEN_HORIZONTAL, false);

        assertFalse(visibleTracks.isMultitrack());
    }

    @Test
    void thePreferenceDoesNothingOutsideHorizontalScreen() {
        visibleTracks.setMultitrack(false);

        HorizontalMultitrack.applyTo(visibleTracks, ViewMode.SCREEN_VERTICAL, true);
        HorizontalMultitrack.applyTo(visibleTracks, ViewMode.PAGE, true);
        HorizontalMultitrack.applyTo(visibleTracks, ViewMode.PARCHMENT, true);

        assertFalse(visibleTracks.isMultitrack());
    }

    @Test
    void turningOffTheMultitrackViewByHandStillWorksAfterForcingIt() {
        HorizontalMultitrack.applyTo(visibleTracks, ViewMode.SCREEN_HORIZONTAL, true);
        assertTrue(visibleTracks.isMultitrack());

        visibleTracks.setMultitrack(false);

        assertFalse(visibleTracks.isMultitrack());
    }
}
