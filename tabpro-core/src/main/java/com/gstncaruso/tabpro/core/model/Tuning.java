package com.gstncaruso.tabpro.core.model;

import java.util.List;

public record Tuning(List<Pitch> strings) {

    public Tuning {
        if (strings.isEmpty()) {
            throw new IllegalArgumentException("una afinacion necesita al menos una cuerda");
        }
        strings = List.copyOf(strings);
    }

    public static Tuning standard() {
        return new Tuning(List.of(
                new Pitch(64),
                new Pitch(59),
                new Pitch(55),
                new Pitch(50),
                new Pitch(45),
                new Pitch(40)));
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
}
