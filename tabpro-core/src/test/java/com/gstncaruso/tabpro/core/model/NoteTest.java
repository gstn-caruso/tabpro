package com.gstncaruso.tabpro.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NoteTest {

    @Test
    void keepsStringAndFret() {
        Note note = new Note(3, 5);
        assertEquals(3, note.string());
        assertEquals(5, note.fret());
    }

    @Test
    void acceptsFretZero() {
        Note note = new Note(1, 0);
        assertEquals(0, note.fret());
    }

    @Test
    void acceptsTheHighestFret() {
        Note note = new Note(1, Note.MAX_FRET);
        assertEquals(Note.MAX_FRET, note.fret());
    }

    @Test
    void rejectsNegativeFrets() {
        assertThrows(IllegalArgumentException.class, () -> new Note(1, -1));
    }

    @Test
    void rejectsFretsAboveTheHighest() {
        assertThrows(IllegalArgumentException.class, () -> new Note(1, Note.MAX_FRET + 1));
    }

    @Test
    void rejectsStringZero() {
        assertThrows(IllegalArgumentException.class, () -> new Note(0, 0));
    }
}
