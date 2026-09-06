package com.gstncaruso.tabpro.format.guitarpro;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * El traste que sigue sonando en cada cuerda de una pista, voz por voz.
 *
 * <p>Guitar Pro escribe cualquier cosa en el traste de una nota ligada -- la que continua
 * a la anterior sin volver a pulsar la cuerda --: el que vale es el de la nota que
 * continua. Como esa nota puede estar en un compas anterior, hay que ir recordandolo
 * mientras se lee la pista entera.
 */
final class GuitarProSoundingFrets {

    private final Map<VoicePart, Map<Integer, Integer>> fretByStringOf = new EnumMap<>(VoicePart.class);

    /** Le devuelve a cada nota ligada el traste de la nota que continua. */
    Voice resolving(VoicePart part, Voice voice) {
        Map<Integer, Integer> sounding = fretByStringOf.computeIfAbsent(part, ignored -> new HashMap<>());
        List<Beat> beats = new ArrayList<>(voice.beatCount());
        for (Beat beat : voice.beats()) {
            beats.add(beat.withNotes(resolved(beat.notes(), sounding)));
        }
        return new Voice(beats);
    }

    private static List<Note> resolved(List<Note> notes, Map<Integer, Integer> sounding) {
        List<Note> played = new ArrayList<>(notes.size());
        for (Note note : notes) {
            played.add(resolved(note, sounding));
        }
        return played;
    }

    /**
     * Si nada suena todavia en esa cuerda, la ligadura no continua nada y no hay mejor
     * traste que el que trae el archivo.
     */
    private static Note resolved(Note note, Map<Integer, Integer> sounding) {
        if (!note.tied()) {
            sounding.put(note.string(), note.fret());
            return note;
        }
        return note.withFret(sounding.getOrDefault(note.string(), note.fret()));
    }
}
