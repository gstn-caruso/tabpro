package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record Beat(Duration duration, List<Note> notes) {

    public Beat {
        List<Note> sorted = new ArrayList<>(notes);
        sorted.sort(Comparator.comparingInt(Note::string));
        notes = List.copyOf(sorted);
    }

    public static Beat rest(Duration duration) {
        return new Beat(duration, List.of());
    }

    public static Beat of(Duration duration, Note... notes) {
        return new Beat(duration, List.of(notes));
    }

    public boolean isRest() {
        return notes.isEmpty();
    }

    public Optional<Note> noteOn(int string) {
        return notes.stream().filter(note -> note.string() == string).findFirst();
    }

    public Beat withNote(Note note) {
        List<Note> updated = new ArrayList<>(notes);
        updated.removeIf(existing -> existing.string() == note.string());
        updated.add(note);
        return new Beat(duration, updated);
    }

    public Beat withoutNoteOn(int string) {
        if (noteOn(string).isEmpty()) {
            return this;
        }
        List<Note> updated = notes.stream().filter(note -> note.string() != string).toList();
        return new Beat(duration, updated);
    }

    public Beat withDuration(Duration duration) {
        return new Beat(duration, notes);
    }
}
