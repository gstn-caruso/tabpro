package com.gstncaruso.tabpro.format.exchange.midi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.model.Note;
import java.util.List;
import org.junit.jupiter.api.Test;

class PercussionChordTest {

    @Test
    void putsEachSoundOnItsOwnLineInAscendingOrder() {
        List<Note> notes = PercussionChord.notesFor(List.of(42, 36));

        assertEquals(List.of(new Note(1, 36), new Note(2, 42)), notes);
    }

    @Test
    void dropsSoundsBeyondTheSixPercussionLines() {
        List<Note> notes = PercussionChord.notesFor(List.of(35, 36, 37, 38, 39, 40, 41));

        assertEquals(6, notes.size());
    }
}
