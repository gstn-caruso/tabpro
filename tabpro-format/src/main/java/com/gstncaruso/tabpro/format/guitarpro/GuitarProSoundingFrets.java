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
 * The fret that keeps sounding on each string of a track, voice by voice.
 *
 * <p>Guitar Pro writes anything in the fret of a tied note -- the one that continues the
 * previous one without plucking the string again --: the one that counts is that of the
 * note it continues. Since that note can be in an earlier measure, it has to be
 * remembered while the whole track is read.
 */
final class GuitarProSoundingFrets {

    private final Map<VoicePart, Map<Integer, Integer>> fretByStringOf = new EnumMap<>(VoicePart.class);

    /** Gives each tied note the fret of the note it continues. */
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
     * If nothing is sounding yet on that string, the tie continues nothing and there is
     * no better fret than the one the file carries.
     */
    private static Note resolved(Note note, Map<Integer, Integer> sounding) {
        if (!note.tied()) {
            sounding.put(note.string(), note.fret());
            return note;
        }
        return note.withFret(sounding.getOrDefault(note.string(), note.fret()));
    }
}
