package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class PlayheadTest {

    @Test
    void aSilentPlayheadIsNowhere() {
        Playhead playhead = Playhead.silent();

        assertTrue(playhead.isSilent());
        assertEquals(Optional.empty(), playhead.on(0));
        assertEquals(OptionalInt.empty(), playhead.measure());
    }

    @Test
    void remembersWhereEachTrackIs() {
        Playhead playhead = Playhead.silent()
                .advancedTo(new BeatPosition(0, 1, 2))
                .advancedTo(new BeatPosition(1, 1, 0));

        assertFalse(playhead.isSilent());
        assertEquals(Optional.of(new BeatPosition(0, 1, 2)), playhead.on(0));
        assertEquals(Optional.of(new BeatPosition(1, 1, 0)), playhead.on(1));
        assertEquals(Optional.empty(), playhead.on(2));
    }

    @Test
    void keepsOnlyTheLatestPositionOfEachTrack() {
        Playhead playhead = Playhead.silent()
                .advancedTo(new BeatPosition(0, 0, 0))
                .advancedTo(new BeatPosition(0, 0, 1));

        assertEquals(Optional.of(new BeatPosition(0, 0, 1)), playhead.on(0));
    }

    @Test
    void theMeasureBeingPlayedIsTheFurthestAnyTrackReached() {
        Playhead playhead = Playhead.silent()
                .advancedTo(new BeatPosition(0, 3, 0))
                .advancedTo(new BeatPosition(1, 1, 0));

        assertEquals(OptionalInt.of(3), playhead.measure());
    }

    @Test
    void isImmutable() {
        Playhead start = Playhead.silent();

        start.advancedTo(new BeatPosition(0, 0, 0));

        assertTrue(start.isSilent());
    }
}
