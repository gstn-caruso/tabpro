package com.gstncaruso.tabpro.ui.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.harmony.Chord;
import com.gstncaruso.tabpro.core.harmony.ChordType;
import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import org.junit.jupiter.api.Test;

class ChordSelectionTest {

    @Test
    void withoutInversionTheBassIsTheRoot() {
        ChordSelection selection = ChordSelection.initial();

        assertEquals(Chord.of(selection.root(), selection.type()), selection.chord());
        assertFalse(selection.chord().isInverted());
    }

    @Test
    void aBassDifferentFromTheRootBuildsAnInvertedChord() {
        ChordSelection selection = ChordSelection.initial().withBass(PitchClass.of("E"));

        assertEquals(
                Chord.inverted(selection.root(), selection.type(), PitchClass.of("E")),
                selection.chord());
        assertTrue(selection.chord().isInverted());
    }

    @Test
    void changingTheRootKeepsTheTypeAndTheFilter() {
        ChordSelection selection = ChordSelection.initial()
                .withType(ChordType.MINOR_SEVENTH)
                .withComplexity(ChordComplexity.SIMPLE)
                .withRoot(PitchClass.of("D"));

        assertEquals(PitchClass.of("D"), selection.root());
        assertEquals(ChordType.MINOR_SEVENTH, selection.type());
        assertEquals(ChordComplexity.SIMPLE, selection.complexity());
    }

    @Test
    void changingTheRootMovesTheBassWithItWhenThereWasNoInversion() {
        ChordSelection selection = ChordSelection.initial().withRoot(PitchClass.of("G"));

        assertEquals(PitchClass.of("G"), selection.bass());
    }

    @Test
    void changingTheRootDoesNotMoveABassThatWasDeliberatelyChosen() {
        ChordSelection selection = ChordSelection.initial()
                .withBass(PitchClass.of("E"))
                .withRoot(PitchClass.of("G"));

        assertEquals(PitchClass.of("E"), selection.bass());
    }
}
