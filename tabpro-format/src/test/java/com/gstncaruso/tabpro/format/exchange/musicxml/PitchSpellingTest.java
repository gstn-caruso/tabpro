package com.gstncaruso.tabpro.format.exchange.musicxml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Pitch;
import org.junit.jupiter.api.Test;

class PitchSpellingTest {

    @Test
    void spellsMiddleCAsC4() {
        PitchSpelling.Spelling spelling = PitchSpelling.spell(new Pitch(60), false);

        assertEquals('C', spelling.step());
        assertEquals(0, spelling.alter());
        assertEquals(4, spelling.octave());
    }

    @Test
    void spellsABlackKeyAsASharpByDefault() {
        PitchSpelling.Spelling spelling = PitchSpelling.spell(new Pitch(61), false);

        assertEquals('C', spelling.step());
        assertEquals(1, spelling.alter());
    }

    @Test
    void spellsABlackKeyAsAFlatWhenAskedTo() {
        PitchSpelling.Spelling spelling = PitchSpelling.spell(new Pitch(61), true);

        assertEquals('D', spelling.step());
        assertEquals(-1, spelling.alter());
    }

    @Test
    void roundTripsThroughStepAlterOctave() {
        Pitch original = new Pitch(66);

        PitchSpelling.Spelling spelling = PitchSpelling.spell(original, false);
        Pitch restored = PitchSpelling.pitchOf(spelling.step(), spelling.alter(), spelling.octave());

        assertEquals(original, restored);
    }

    @Test
    void thelowestGuitarOpenStringIsE2() {
        PitchSpelling.Spelling spelling = PitchSpelling.spell(new Pitch(40), false);

        assertEquals('E', spelling.step());
        assertEquals(0, spelling.alter());
        assertEquals(2, spelling.octave());
    }
}
