package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.effects.Velocity;
import org.junit.jupiter.api.Test;

class ScheduledNoteTest {

    @Test
    void aNoteThatKeepsItsPitchIsPlayedClean() {
        ScheduledNote clean = new ScheduledNote(0, 960, new Pitch(64));

        assertFalse(clean.carriesAnEffect());
    }

    @Test
    void aNoteThatMovesItsPitchCarriesAnEffect() {
        ScheduledNote withBend = new ScheduledNote(0, 960, new Pitch(64), new Velocity(100),
                PitchTrajectory.ramp(0, 0, 960, 2), false);

        assertTrue(withBend.carriesAnEffect());
    }

    @Test
    void aNoteThatFadesInCarriesAnEffect() {
        ScheduledNote withFadeIn = new ScheduledNote(0, 960, new Pitch(64), new Velocity(100),
                PitchTrajectory.flat(), true);

        assertTrue(withFadeIn.carriesAnEffect());
    }
}
