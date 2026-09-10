package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IntervalTest {

    @Test
    void theRootMovesNothing() {
        assertEquals(0, Interval.ROOT.letterSteps());
        assertEquals(0, Interval.ROOT.semitones());
    }

    @Test
    void theMajorThirdIsFourSemitonesAndTwoLetters() {
        assertEquals(2, Interval.MAJOR_THIRD.letterSteps());
        assertEquals(4, Interval.MAJOR_THIRD.semitones());
    }

    @Test
    void theDiminishedFifthAndTheAugmentedFourthSoundTheSameButAreWrittenDifferently() {
        assertEquals(Interval.DIMINISHED_FIFTH.semitones(), Interval.AUGMENTED_FOURTH.semitones());
        assertEquals(4, Interval.DIMINISHED_FIFTH.letterSteps());
        assertEquals(3, Interval.AUGMENTED_FOURTH.letterSteps());
    }

    @Test
    void appliedToARootGivesTheExpectedNote() {
        PitchClass c = PitchClass.of("Do");
        assertEquals(PitchClass.of("Mi"), Interval.MAJOR_THIRD.from(c));
        assertEquals(PitchClass.of("Sol"), Interval.PERFECT_FIFTH.from(c));
        assertEquals(PitchClass.of("Sib"), Interval.MINOR_SEVENTH.from(c));
        assertEquals(PitchClass.of("Si"), Interval.MAJOR_SEVENTH.from(c));
    }

    @Test
    void theNinthAndTheSecondSoundTheSameButHaveADifferentLabel() {
        assertEquals(Interval.MAJOR_SECOND.semitones(), Interval.MAJOR_NINTH.semitones() % 12);
        assertEquals("2", Interval.MAJOR_SECOND.label());
        assertEquals("9", Interval.MAJOR_NINTH.label());
    }

    @Test
    void findsTheIntervalByStepsAndSemitones() {
        assertEquals(Interval.MAJOR_THIRD, Interval.matching(2, 4).orElseThrow());
        assertEquals(Interval.MINOR_THIRD, Interval.matching(2, 3).orElseThrow());
    }

    @Test
    void theDegreeIsTheNumberAMusicianNamesItWithoutTheAccidental() {
        assertEquals(1, Interval.ROOT.degreeNumber());
        assertEquals(3, Interval.MINOR_THIRD.degreeNumber());
        assertEquals(3, Interval.MAJOR_THIRD.degreeNumber());
        assertEquals(5, Interval.PERFECT_FIFTH.degreeNumber());
        assertEquals(7, Interval.MINOR_SEVENTH.degreeNumber());
    }

    @Test
    void extensionsUseTheDegreeBeyondOneOctave() {
        assertEquals(9, Interval.MAJOR_NINTH.degreeNumber());
        assertEquals(11, Interval.PERFECT_ELEVENTH.degreeNumber());
        assertEquals(13, Interval.MAJOR_THIRTEENTH.degreeNumber());
    }
}
