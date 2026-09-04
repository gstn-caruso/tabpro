package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BeatTest {

    @Test
    void aBeatWithoutNotesIsARest() {
        assertTrue(Beat.rest(Duration.quarter()).isRest());
    }

    @Test
    void aBeatWithANoteIsNotARest() {
        assertFalse(Beat.of(Duration.quarter(), new Note(1, 0)).isRest());
    }

    @Test
    void replacesTheNoteOnTheSameString() {
        Beat beat = Beat.of(Duration.quarter(), new Note(3, 0));
        Beat replaced = beat.withNote(new Note(3, 5));
        assertEquals(Optional.of(new Note(3, 5)), replaced.noteOn(3));
        assertEquals(1, replaced.notes().size());
    }

    @Test
    void keepsNotesOnOtherStrings() {
        Beat beat = Beat.of(Duration.quarter(), new Note(3, 0), new Note(2, 1));
        Beat replaced = beat.withNote(new Note(3, 5));
        assertEquals(Optional.of(new Note(2, 1)), replaced.noteOn(2));
    }

    @Test
    void exposesNotesOrderedByString() {
        Beat beat = Beat.of(Duration.quarter(), new Note(5, 0), new Note(1, 2), new Note(3, 1));
        assertEquals(List.of(new Note(1, 2), new Note(3, 1), new Note(5, 0)), beat.notes());
    }

    @Test
    void removingTheOnlyNoteMakesItARest() {
        Beat beat = Beat.of(Duration.quarter(), new Note(1, 0));
        assertTrue(beat.withoutNoteOn(1).isRest());
    }

    @Test
    void removingANoteFromAnEmptyStringChangesNothing() {
        Beat beat = Beat.of(Duration.quarter(), new Note(1, 0));
        assertEquals(beat, beat.withoutNoteOn(2));
    }

    @Test
    void changesDurationKeepingNotes() {
        Beat beat = Beat.of(Duration.quarter(), new Note(1, 0));
        Beat changed = beat.withDuration(Duration.quarter().longer());
        assertEquals(Duration.quarter().longer(), changed.duration());
        assertEquals(beat.notes(), changed.notes());
    }

    @Test
    void exposesAnUnmodifiableNoteList() {
        Beat beat = Beat.of(Duration.quarter(), new Note(1, 0));
        assertThrows(UnsupportedOperationException.class, () -> beat.notes().add(new Note(2, 0)));
    }
}
