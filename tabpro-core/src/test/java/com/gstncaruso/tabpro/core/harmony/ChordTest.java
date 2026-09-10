package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ChordTest {

    @Test
    void cMajorIsCEG() {
        Chord cMajor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        assertEquals(
                List.of(PitchClass.of("Do"), PitchClass.of("Mi"), PitchClass.of("Sol")), cMajor.pitchClasses());
    }

    @Test
    void aMinorIsACE() {
        Chord aMinor = Chord.of(PitchClass.of("La"), ChordType.MINOR);
        assertEquals(
                List.of(PitchClass.of("La"), PitchClass.of("Do"), PitchClass.of("Mi")), aMinor.pitchClasses());
    }

    @Test
    void withoutInversionTheBassIsTheRoot() {
        Chord cMajor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        assertFalse(cMajor.isInverted());
        assertEquals(PitchClass.of("Do"), cMajor.bass());
    }

    @Test
    void anInversionChangesTheBassWithoutChangingTheNotes() {
        Chord cWithEInTheBass = Chord.inverted(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Mi"));
        assertTrue(cWithEInTheBass.isInverted());
        assertEquals(PitchClass.of("Mi"), cWithEInTheBass.bass());
        assertEquals(
                List.of(PitchClass.of("Do"), PitchClass.of("Mi"), PitchClass.of("Sol")),
                cWithEInTheBass.pitchClasses());
    }

    @Test
    void theNameCarriesTheRootAndTheSuffix() {
        assertEquals("C", Chord.of(PitchClass.of("Do"), ChordType.MAJOR).name());
        assertEquals("Am", Chord.of(PitchClass.of("La"), ChordType.MINOR).name());
        assertEquals("G7", Chord.of(PitchClass.of("Sol"), ChordType.SEVENTH).name());
    }

    @Test
    void anInversionIsWrittenWithASlash() {
        Chord cWithEInTheBass = Chord.inverted(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Mi"));
        assertEquals("C/E", cWithEInTheBass.name());
    }

    @Test
    void essentialSemitonesIncludeTheBassEvenWhenItIsForeignToTheChord() {
        Chord cWithDInTheBass = Chord.inverted(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Re"));
        assertTrue(cWithDInTheBass.essentialSemitones().contains(PitchClass.of("Re").semitone()));
    }

    @Test
    void theFullFormulaIncludesTheOptionalTones() {
        Chord g7 = Chord.of(PitchClass.of("Sol"), ChordType.SEVENTH);
        assertTrue(g7.formulaSemitones().contains(PitchClass.of("Re").semitone()));
    }

    @Test
    void thePreferenceCanWriteTheInversionWithoutTheBass() {
        Chord cWithEInTheBass = Chord.inverted(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Mi"));
        assertEquals("C", cWithEInTheBass.name(false));
        assertEquals("C/E", cWithEInTheBass.name(true));
    }

    @Test
    void aChordWithoutInversionIsWrittenTheSameWithAnyPreference() {
        Chord cMajor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        assertEquals("C", cMajor.name(false));
        assertEquals("C", cMajor.name(true));
    }

    @Test
    void omittingAToneDropsItFromTheEssentialSemitones() {
        Chord cMajor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        assertFalse(cMajor.essentialSemitones(Set.of(Interval.PERFECT_FIFTH)).contains(PitchClass.of("Sol").semitone()));
        assertTrue(cMajor.essentialSemitones(Set.of()).contains(PitchClass.of("Sol").semitone()));
    }

    @Test
    void theBassStaysEssentialEvenWhenChordTonesAreOmitted() {
        Chord cWithDInTheBass = Chord.inverted(PitchClass.of("Do"), ChordType.MAJOR, PitchClass.of("Re"));
        assertTrue(cWithDInTheBass.essentialSemitones(Set.of(Interval.MAJOR_THIRD, Interval.PERFECT_FIFTH))
                .contains(PitchClass.of("Re").semitone()));
    }

    @Test
    void omittingAnIntervalTheChordDoesNotHaveChangesNothing() {
        Chord cMajor = Chord.of(PitchClass.of("Do"), ChordType.MAJOR);
        assertEquals(cMajor.essentialSemitones(Set.of()), cMajor.essentialSemitones(Set.of(Interval.MINOR_SEVENTH)));
    }
}
