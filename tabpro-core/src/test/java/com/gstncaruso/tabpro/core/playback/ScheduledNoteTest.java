package com.gstncaruso.tabpro.core.playback;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.effects.Velocity;
import org.junit.jupiter.api.Test;

class ScheduledNoteTest {

    @Test
    void aNoteThatKeepsItsPitchIsPlayedClean() {
        ScheduledNote limpia = new ScheduledNote(0, 960, new Pitch(64));

        assertFalse(limpia.carriesAnEffect());
    }

    @Test
    void aNoteThatMovesItsPitchCarriesAnEffect() {
        ScheduledNote conBend = new ScheduledNote(0, 960, new Pitch(64), new Velocity(100),
                PitchTrajectory.ramp(0, 0, 960, 2), false);

        assertTrue(conBend.carriesAnEffect());
    }

    @Test
    void aNoteThatFadesInCarriesAnEffect() {
        ScheduledNote conFadeIn = new ScheduledNote(0, 960, new Pitch(64), new Velocity(100),
                PitchTrajectory.flat(), true);

        assertTrue(conFadeIn.carriesAnEffect());
    }
}
