package com.gstncaruso.tabpro.format;

import com.gstncaruso.tabpro.core.model.Note;

public record NoteDto(int string, int fret) {

    public static NoteDto from(Note note) {
        return new NoteDto(note.string(), note.fret());
    }

    public Note toNote() {
        return new Note(string, fret);
    }
}
