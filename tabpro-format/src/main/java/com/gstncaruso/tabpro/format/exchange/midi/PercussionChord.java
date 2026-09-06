package com.gstncaruso.tabpro.format.exchange.midi;

import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.PercussionKit;
import java.util.ArrayList;
import java.util.List;

/**
 * Ubica los golpes simultaneos de una pista de percusion en las lineas de la tablatura de
 * bateria. En percusion el traste ya es el sonido de General MIDI (Note.fret); la linea es
 * solo para que dos golpes a la vez no compartan renglon, PercussionKit.LINE_COUNT como mucho.
 */
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
