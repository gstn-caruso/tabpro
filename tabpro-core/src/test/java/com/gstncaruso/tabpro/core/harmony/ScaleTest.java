package com.gstncaruso.tabpro.core.harmony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScaleTest {

    private static final Scale MAJOR = new Scale("Mayor", List.of(0, 2, 4, 5, 7, 9, 11), List.of(0, 1, 2, 3, 4, 5, 6));

    @Test
    void cMajorIsTheSevenNaturals() {
        assertEquals(
                List.of("C", "D", "E", "F", "G", "A", "B"),
                MAJOR.notesFrom(PitchClass.of("C")).stream().map(note -> note.pitchClass().name()).toList());
    }

    @Test
    void theDegreeIsThePositionInTheScale() {
        List<ScaleTone> notes = MAJOR.notesFrom(PitchClass.of("C"));
        assertEquals(1, notes.get(0).degree());
        assertEquals(5, notes.get(4).degree());
        assertEquals(7, notes.get(6).degree());
    }

    @Test
    void theIntervalIsRelativeToTheTonic() {
        List<ScaleTone> notes = MAJOR.notesFrom(PitchClass.of("C"));
        assertEquals(Interval.MAJOR_THIRD, notes.get(2).interval());
        assertEquals(Interval.PERFECT_FIFTH, notes.get(4).interval());
    }

    @Test
    void dMajorCarriesSharps() {
        assertEquals(
                List.of("D", "E", "F#", "G", "A", "B", "C#"),
                MAJOR.notesFrom(PitchClass.of("D")).stream().map(note -> note.pitchClass().name()).toList());
    }

    @Test
    void rejectsListsOfDifferentSize() {
        assertThrows(IllegalArgumentException.class, () -> new Scale("Mala", List.of(0, 2, 4), List.of(0, 1)));
    }

    @Test
    void rejectsAnEmptyScale() {
        assertThrows(IllegalArgumentException.class, () -> new Scale("Vacia", List.of(), List.of()));
    }

    @Test
    void expressesSemitonesAsPitchClasses() {
        assertEquals(List.of(0, 2, 4, 5, 7, 9, 11), MAJOR.semitones());
        assertEquals(7, MAJOR.degreeCount());
    }
}
