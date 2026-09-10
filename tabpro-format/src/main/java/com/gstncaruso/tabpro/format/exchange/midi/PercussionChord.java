package com.gstncaruso.tabpro.format.exchange.midi;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import java.util.ArrayList;
import java.util.List;

final class PercussionChord {

    private PercussionChord() {
    }

    static List<Note> notesFor(List<Integer> sounds) {
        List<Integer> ordered = sounds.stream().sorted().toList();
        List<Note> notes = new ArrayList<>();
        for (int line = 1; line <= ordered.size() && line <= PercussionKit.LINE_COUNT; line++) {
            notes.add(new Note(line, ordered.get(line - 1)));
        }
        return notes;
    }
}
