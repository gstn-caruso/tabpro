package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class ChordFretting {

    private ChordFretting() {
    }

    public static List<Note> assign(Tuning tuning, int fretCount, List<Pitch> pitches) {
        Set<Integer> usedStrings = new HashSet<>();
        List<Note> notes = new ArrayList<>();
        for (Pitch pitch : pitches) {
            candidatesFor(tuning, fretCount, pitch, usedStrings).findFirst().ifPresent(note -> {
                usedStrings.add(note.string());
                notes.add(note);
            });
        }
        return notes;
    }

    private static Stream<Note> candidatesFor(Tuning tuning, int fretCount, Pitch pitch, Set<Integer> excludedStrings) {
        return IntStream.rangeClosed(1, tuning.stringCount())
                .boxed()
                .filter(string -> !excludedStrings.contains(string))
                .flatMap(string -> tuning.noteFor(pitch, string).stream())
                .filter(note -> note.fret() <= fretCount)
                .sorted(Comparator.comparingInt(Note::fret).thenComparingInt(Note::string));
    }
}
