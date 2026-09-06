package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ChordFrettingTest {

    private final Tuning standard = Tuning.standard();
    private static final int FRET_COUNT = 24;

    @Test
    void placesASinglePitchOnItsMostComfortableString() {
        List<Note> notes = ChordFretting.assign(standard, FRET_COUNT, List.of(new Pitch(55)));

        assertEquals(List.of(new Note(3, 0)), notes);
    }

    @Test
    void discardsAPitchThatDoesNotFitAnyString() {
        List<Note> notes = ChordFretting.assign(standard, FRET_COUNT, List.of(new Pitch(39)));

        assertTrue(notes.isEmpty());
    }

    @Test
    void respectsTheConfiguredFretCount() {
        // 64 + 24 = 88: entraria en la cuerda 1 con el traste 24 al limite, pero no con 12.
        List<Note> withinLimit = ChordFretting.assign(standard, 24, List.of(new Pitch(88)));
        List<Note> beyondLimit = ChordFretting.assign(standard, 12, List.of(new Pitch(88)));

        assertEquals(List.of(new Note(1, 24)), withinLimit);
        assertTrue(beyondLimit.isEmpty());
    }

    @Test
    void assignsEachPitchOfAChordToADifferentString() {
        List<Note> chord = ChordFretting.assign(standard, FRET_COUNT, List.of(new Pitch(64), new Pitch(59), new Pitch(40)));

        assertEquals(3, chord.size());
        assertEquals(3, stringsOf(chord).stream().distinct().count());
    }

    @Test
    void resolvesAStringConflictInAChordWithTheNextBestString() {
        // 45 (la) sale abierta en la cuerda 5, o traste 5 en la cuerda 6: dos notas identicas a la
        // vez se reparten esas dos opciones en vez de pisarse la misma cuerda.
        List<Note> chord = ChordFretting.assign(standard, FRET_COUNT, List.of(new Pitch(45), new Pitch(45)));

        assertEquals(List.of(new Note(5, 0), new Note(6, 5)), chord);
    }

    @Test
    void dropsAChordNoteThatCannotBePlacedAnywhereElse() {
        List<Note> chord = ChordFretting.assign(standard, FRET_COUNT, List.of(new Pitch(40), new Pitch(40)));

        assertEquals(List.of(new Note(6, 0)), chord);
    }

    private static List<Integer> stringsOf(List<Note> notes) {
        return notes.stream().map(Note::string).toList();
    }
}
