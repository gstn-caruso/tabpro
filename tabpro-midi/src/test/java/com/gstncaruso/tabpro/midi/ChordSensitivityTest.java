package com.gstncaruso.tabpro.midi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * La sensibilidad de "Enter Notes > Using a MIDI Instrument": cuanto puede
 * tardar la segunda nota de un acorde antes de que la captura la mande a
 * un beat nuevo.
 */
class ChordSensitivityTest {

    @Test
    void theVeryFirstNoteNeverJoinsAChordThatDoesNotExistYet() {
        ChordSensitivity sensitivity = new ChordSensitivity(60);

        assertFalse(sensitivity.sameChordAt(0));
    }

    @Test
    void aNoteWellWithinTheSensitivityJoinsTheSameChord() {
        ChordSensitivity sensitivity = new ChordSensitivity(60);
        sensitivity.sameChordAt(1_000);

        assertTrue(sensitivity.sameChordAt(1_030));
    }

    @Test
    void aNoteExactlyAtTheSensitivityStillJoinsTheSameChord() {
        ChordSensitivity sensitivity = new ChordSensitivity(60);
        sensitivity.sameChordAt(1_000);

        assertTrue(sensitivity.sameChordAt(1_060));
    }

    @Test
    void aNoteBeyondTheSensitivityOpensANewBeat() {
        ChordSensitivity sensitivity = new ChordSensitivity(60);
        sensitivity.sameChordAt(1_000);

        assertFalse(sensitivity.sameChordAt(1_061));
    }

    @Test
    void aTighterSensitivitySplitsNotesThatALooserOneWouldHaveJoined() {
        ChordSensitivity strict = new ChordSensitivity(10);
        strict.sameChordAt(1_000);

        assertFalse(strict.sameChordAt(1_030));
    }

    @Test
    void eachNoteBecomesTheReferenceForTheNextOne() {
        ChordSensitivity sensitivity = new ChordSensitivity(60);
        sensitivity.sameChordAt(1_000);
        sensitivity.sameChordAt(1_050);

        assertFalse(sensitivity.sameChordAt(1_200));
    }
}
