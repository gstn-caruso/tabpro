package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScaleLibraryTest {

    private static List<String> namesOf(Scale scale, String tonic) {
        return scale.notesFrom(PitchClass.of(tonic)).stream().map(note -> note.pitchClass().name()).toList();
    }

    @Test
    void majorIsCDEFGAB() {
        assertEquals(List.of("C", "D", "E", "F", "G", "A", "B"), namesOf(ScaleLibrary.major(), "C"));
    }

    @Test
    void dDorianIsTheRestOfTheWhiteKeys() {
        assertEquals(List.of("D", "E", "F", "G", "A", "B", "C"), namesOf(ScaleLibrary.dorian(), "D"));
    }

    @Test
    void aNaturalMinorIsTheRestOfTheWhiteKeys() {
        assertEquals(List.of("A", "B", "C", "D", "E", "F", "G"), namesOf(ScaleLibrary.naturalMinor(), "A"));
    }

    @Test
    void harmonicMinorCarriesTheNaturalMajorLeadingTone() {
        assertEquals(List.of("A", "B", "C", "D", "E", "F", "G#"), namesOf(ScaleLibrary.harmonicMinor(), "A"));
    }

    @Test
    void melodicMinorOnlyAltersTheThird() {
        assertEquals(List.of("A", "B", "C", "D", "E", "F#", "G#"), namesOf(ScaleLibrary.melodicMinor(), "A"));
    }

    @Test
    void majorPentatonicIsFiveNotesWithoutFourthOrSeventh() {
        assertEquals(List.of("C", "D", "E", "G", "A"), namesOf(ScaleLibrary.majorPentatonic(), "C"));
    }

    @Test
    void minorPentatonicIsFiveNotesWithMinorThirdAndSeventh() {
        assertEquals(List.of("A", "C", "D", "E", "G"), namesOf(ScaleLibrary.minorPentatonic(), "A"));
    }

    @Test
    void bluesAddsTheDiminishedFifthToTheMinorPentatonic() {
        assertEquals(List.of("C", "Eb", "F", "Gb", "G", "Bb"), namesOf(ScaleLibrary.blues(), "C"));
    }

    @Test
    void wholeToneIsSixEquidistantNotes() {
        assertEquals(List.of("C", "D", "E", "F#", "G#", "A#"), namesOf(ScaleLibrary.wholeTone(), "C"));
    }

    @Test
    void chromaticHasTheTwelveNotes() {
        assertEquals(12, ScaleLibrary.chromatic().degreeCount());
        assertEquals(
                List.of("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"),
                namesOf(ScaleLibrary.chromatic(), "C"));
    }

    @Test
    void wholeHalfDiminishedHasEightNotes() {
        assertEquals(
                List.of("C", "D", "Eb", "F", "Gb", "Ab", "A", "B"),
                namesOf(ScaleLibrary.diminishedWholeHalf(), "C"));
    }

    @Test
    void dominantDiminishedIsHalfWhole() {
        assertEquals(
                List.of("C", "Db", "Eb", "E", "F#", "G", "A", "Bb"),
                namesOf(ScaleLibrary.diminishedHalfWhole(), "C"));
    }

    @Test
    void theSpanishScaleIsAPhrygianWithAMajorThird() {
        assertEquals(
                List.of("E", "F", "G#", "A", "B", "C", "D"), namesOf(ScaleLibrary.phrygianDominant(), "E"));
    }

    @Test
    void everyScaleIsSpelledWithoutRepeatingOrSkippingLetters() {
        for (Scale scale : ScaleLibrary.all()) {
            List<ScaleTone> notes = scale.notesFrom(PitchClass.of("C"));
            assertEquals(scale.degreeCount(), notes.size(), scale.id());
        }
    }

    @Test
    void offersAWideLibrary() {
        assertTrue(ScaleLibrary.all().size() >= 20);
    }
}
