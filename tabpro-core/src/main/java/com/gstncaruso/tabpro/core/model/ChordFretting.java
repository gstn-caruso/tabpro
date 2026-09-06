package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Reparte un grupo de alturas reales (MIDI, MusicXML sin tablatura) entre las cuerdas de una
 * afinacion. Tuning ya sabe la cuerda mas comoda para una altura sola (bestNoteFor); esta clase
 * se ocupa de lo que pasa cuando varias alturas suenan juntas y compiten por la misma cuerda.
 */
public final class ChordFretting {

    private ChordFretting() {
    }

    /**
     * Cada altura se queda con la cuerda mas comoda (menor traste) que siga libre; la primera
     * altura del acorde tiene prioridad. Una altura que no entra en ninguna cuerda libre, dentro
     * del limite de trastes de la pista, se descarta, como avisa el manual.
     */
    public static List<Note> assign(Tuning tuning, int fretCount, List<Pitch> pitches) {
        Set<Integer> usedStrings = new HashSet<>();
        List<Note> notes = new ArrayList<>();
        for (Pitch pitch : pitches) {
            candidatesFor(tuning, fretCount, pitch, usedStrings).findFirst().ifPresent(note -> {
                usedStrings.add(note.string());
                notes.add(note);
            });
        }
        return notes;
    }

    private static Stream<Note> candidatesFor(Tuning tuning, int fretCount, Pitch pitch, Set<Integer> excludedStrings) {
        return IntStream.rangeClosed(1, tuning.stringCount())
                .boxed()
                .filter(string -> !excludedStrings.contains(string))
                .flatMap(string -> tuning.noteFor(pitch, string).stream())
                .filter(note -> note.fret() <= fretCount)
                .sorted(Comparator.comparingInt(Note::fret).thenComparingInt(Note::string));
    }
}
