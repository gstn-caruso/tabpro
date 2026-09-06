package com.gstncaruso.tabpro.format.exchange.musicxml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TieStartsTest {

    @Test
    void marksTheFirstNoteOfATieAsItsStart() {
        Beat attack = Beat.of(Duration.of(NoteValue.QUARTER), new Note(6, 0));
        Beat tied = Beat.of(Duration.of(NoteValue.QUARTER), Note.tiedOn(6));
        List<Beat> beats = List.of(attack, tied);

        Map<Integer, Set<Integer>> starts = TieStarts.of(beats);

        assertEquals(Set.of(6), starts.get(0));
        assertTrue(starts.getOrDefault(1, Set.of()).isEmpty());
    }

    @Test
    void chainsThreeTiedNotesTogether() {
        Beat first = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 5));
        Beat second = Beat.of(Duration.of(NoteValue.QUARTER), Note.tiedOn(1));
        Beat third = Beat.of(Duration.of(NoteValue.QUARTER), Note.tiedOn(1));
        List<Beat> beats = List.of(first, second, third);

        Map<Integer, Set<Integer>> starts = TieStarts.of(beats);

        assertEquals(Set.of(1), starts.get(0));
        assertEquals(Set.of(1), starts.get(1));
    }

    @Test
    void doesNotTieAcrossAnUnrelatedNoteOnTheSameString() {
        Beat first = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 5));
        Beat other = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 7));
        List<Beat> beats = List.of(first, other);

        Map<Integer, Set<Integer>> starts = TieStarts.of(beats);

        assertTrue(starts.getOrDefault(0, Set.of()).isEmpty());
    }

    @Test
    void skipsOverRestsToFindThePreviousNoteOnTheString() {
        Beat first = Beat.of(Duration.of(NoteValue.QUARTER), new Note(1, 5));
        Beat rest = Beat.rest(Duration.of(NoteValue.QUARTER));
        Beat tied = Beat.of(Duration.of(NoteValue.QUARTER), Note.tiedOn(1));
        List<Beat> beats = List.of(first, rest, tied);

        Map<Integer, Set<Integer>> starts = TieStarts.of(beats);

        assertEquals(Set.of(1), starts.get(0));
    }
}
