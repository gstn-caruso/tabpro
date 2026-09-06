package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Como esta afinada cada cuerda. La cuerda 1 es la mas aguda. */
public record Tuning(String name, List<Pitch> strings) {

    /** El traste mas alto que tiene un instrumento de cuerda en este programa. */
    public static final int MAX_FRET = 36;

    public Tuning {
        if (strings.isEmpty()) {
            throw new IllegalArgumentException("una afinacion necesita al menos una cuerda");
        }
        strings = List.copyOf(strings);
    }

    public Tuning(List<Pitch> strings) {
        this("Personalizada", strings);
    }

    public static Tuning of(String name, int... midiNumbers) {
        List<Pitch> pitches = new ArrayList<>();
        for (int midiNumber : midiNumbers) {
            pitches.add(new Pitch(midiNumber));
        }
        return new Tuning(name, pitches);
    }

    public static Tuning standard() {
        return TuningLibrary.standardGuitar();
    }

    public static Tuning standardBass() {
        return TuningLibrary.standardBass();
    }

    public int stringCount() {
        return strings.size();
    }

    public Pitch pitchOfString(int string) {
        if (string < 1 || string > stringCount()) {
            throw new IllegalArgumentException("string fuera de rango: " + string);
        }
        return strings.get(string - 1);
    }

    public Pitch pitchOf(Note note) {
        return pitchOfString(note.string()).transposed(note.fret());
    }

    /** La nota que suena asi de aguda en esa cuerda, si es que la cuerda llega. */
    public Optional<Note> noteFor(Pitch pitch, int string) {
        int fret = pitch.midiNumber() - pitchOfString(string).midiNumber();
        if (fret < 0 || fret > MAX_FRET) {
            return Optional.empty();
        }
        return Optional.of(new Note(string, fret));
    }

    /** La cuerda mas aguda que pueda tocar esa altura sin pasarse de trastes. */
    public Optional<Note> bestNoteFor(Pitch pitch, int fretCount) {
        for (int string = 1; string <= stringCount(); string++) {
            Optional<Note> candidate = noteFor(pitch, string);
            if (candidate.isPresent() && candidate.get().fret() <= fretCount) {
                return candidate;
            }
        }
        return Optional.empty();
    }

    public Tuning withStringPitch(int string, Pitch pitch) {
        List<Pitch> updated = new ArrayList<>(strings);
        updated.set(string - 1, pitch);
        return new Tuning("Personalizada", updated);
    }

    public Tuning withStringCount(int count) {
        if (count == stringCount()) {
            return this;
        }
        List<Pitch> updated = new ArrayList<>(strings.subList(0, Math.min(count, stringCount())));
        while (updated.size() < count) {
            updated.add(updated.getLast().transposed(-5));
        }
        return new Tuning("Personalizada", updated);
    }

    public Tuning transposed(int semitones) {
        return new Tuning(name, strings.stream().map(pitch -> pitch.transposed(semitones)).toList());
    }
}
