package com.gstncaruso.tabpro.core.model;

import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Un golpe de la partitura: una figura, las notas que suenan en el, y sus efectos. */
public record Beat(Duration duration, List<Note> notes, BeatEffects effects) {

    public Beat {
        List<Note> sorted = new ArrayList<>(notes);
        sorted.sort(Comparator.comparingInt(Note::string));
        notes = List.copyOf(sorted);
    }

    public Beat(Duration duration, List<Note> notes) {
        this(duration, notes, BeatEffects.none());
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

    /** La cuerda mas grave que suena, que es la de numero mas alto. */
    public Optional<Note> lowestNote() {
        return notes.stream().max(Comparator.comparingInt(Note::string));
    }

    public Beat withNote(Note note) {
        List<Note> updated = new ArrayList<>(notes);
        updated.removeIf(existing -> existing.string() == note.string());
        updated.add(note);
        return withNotes(updated);
    }

    public Beat withoutNoteOn(int string) {
        if (noteOn(string).isEmpty()) {
            return this;
        }
        return withNotes(notes.stream().filter(note -> note.string() != string).toList());
    }

    public Beat mappingNoteOn(int string, java.util.function.UnaryOperator<Note> change) {
        return noteOn(string).map(note -> withNote(change.apply(note))).orElse(this);
    }

    public Beat mappingEveryNote(java.util.function.UnaryOperator<Note> change) {
        return withNotes(notes.stream().map(change).toList());
    }

    public Beat withDuration(Duration duration) {
        return new Beat(duration, notes, effects);
    }

    public Beat withEffects(BeatEffects effects) {
        return new Beat(duration, notes, effects);
    }

    public Beat withNotes(List<Note> notes) {
        return new Beat(duration, notes, effects);
    }
}
