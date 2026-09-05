package com.gstncaruso.tabpro.format;

import java.util.Arrays;
import java.util.List;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;

public record BeatDto(int value, boolean dotted, List<NoteDto> notes) {

    public static BeatDto from(Beat beat) {
        List<NoteDto> notes = beat.notes().stream().map(NoteDto::from).toList();
        return new BeatDto(beat.duration().value().denominator(), beat.duration().dotted(), notes);
    }

    public Beat toBeat() {
        NoteValue noteValue = noteValueOf(value);
        List<Note> domainNotes = notes.stream().map(NoteDto::toNote).toList();
        return new Beat(new Duration(noteValue, dotted), domainNotes);
    }

    private static NoteValue noteValueOf(int denominator) {
        return Arrays.stream(NoteValue.values())
                .filter(candidate -> candidate.denominator() == denominator)
                .findFirst()
                .orElseThrow();
    }
}
