package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TuningTest {

    @Test
    void standardTuningHasSixStrings() {
        assertEquals(6, Tuning.standard().stringCount());
    }

    @Test
    void standardTuningStringOneIsHighE() {
        assertEquals(new Pitch(64), Tuning.standard().pitchOfString(1));
    }

    @Test
    void standardTuningStringSixIsLowE() {
        assertEquals(new Pitch(40), Tuning.standard().pitchOfString(6));
    }

    @Test
    void pitchOfAnOpenStringIsTheStringPitch() {
        assertEquals(new Pitch(50), Tuning.standard().pitchOf(new Note(4, 0)));
    }

    @Test
    void pitchOfAFrettedNoteAddsTheFretToTheStringPitch() {
        assertEquals(new Pitch(52), Tuning.standard().pitchOf(new Note(4, 2)));
    }

    @Test
    void findsTheFretThatSoundsAPitchOnAString() {
        assertEquals(Optional.of(new Note(3, 5)), Tuning.standard().noteFor(new Pitch(60), 3));
    }

    @Test
    void aPitchThatIsTheStringItselfIsPlayedOpen() {
        assertEquals(Optional.of(new Note(1, 0)), Tuning.standard().noteFor(new Pitch(64), 1));
    }

    @Test
    void aPitchBelowTheStringIsOutOfItsReach() {
        assertEquals(Optional.empty(), Tuning.standard().noteFor(new Pitch(40), 1));
    }

    @Test
    void theLastFretIsStillWithinReach() {
        assertEquals(
                Optional.of(new Note(1, Note.MAX_FRET)),
                Tuning.standard().noteFor(new Pitch(64 + Note.MAX_FRET), 1));
    }

    @Test
    void aPitchPastTheLastFretIsOutOfReach() {
        assertEquals(Optional.empty(), Tuning.standard().noteFor(new Pitch(64 + Note.MAX_FRET + 1), 1));
    }

    @Test
    void aBassReachesOnItsOwnStrings() {
        assertEquals(Optional.of(new Note(4, 3)), Tuning.standardBass().noteFor(new Pitch(31), 4));
        assertEquals(Optional.empty(), Tuning.standardBass().noteFor(new Pitch(31), 1));
    }

    @Test
    void rejectsAStringItDoesNotHaveWhenLookingForAFret() {
        assertThrows(IllegalArgumentException.class, () -> Tuning.standard().noteFor(new Pitch(60), 7));
    }

    @Test
    void rejectsStringZero() {
        assertThrows(IllegalArgumentException.class, () -> Tuning.standard().pitchOfString(0));
    }

    @Test
    void rejectsAStringBeyondTheLast() {
        assertThrows(IllegalArgumentException.class, () -> Tuning.standard().pitchOfString(7));
    }

    @Test
    void rejectsATuningWithoutStrings() {
        assertThrows(IllegalArgumentException.class, () -> new Tuning(List.of()));
    }
}
