package com.gstncaruso.tabpro.core.model.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SoundDurationTest {

    @Test
    void aPlainNoteSoundsItsWholeFigure() {
        assertEquals(1.0, NoteEffects.none().soundLength(), 0.001);
    }

    @Test
    void halfTheSoundDurationSoundsHalfTheFigure() {
        assertEquals(0.5, NoteEffects.none().withSoundDuration(50).soundLength(), 0.001);
    }

    @Test
    void theSoundDurationMultipliesWhatTheOrnamentAlreadyDecided() {
        NoteEffects staccato = NoteEffects.none().with(Ornament.STACCATO);

        assertEquals(staccato.soundLength() / 2, staccato.withSoundDuration(50).soundLength(), 0.001);
    }

    @Test
    void aDurationOutOfRangeIsBroughtBackIn() {
        assertTrue(NoteEffects.none().withSoundDuration(0).soundDurationPercent() >= 1);
        assertTrue(NoteEffects.none().withSoundDuration(1000).soundDurationPercent() <= 200);
    }
}
