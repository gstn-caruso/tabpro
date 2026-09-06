package com.gstncaruso.tabpro.core.harmony;

import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * La zona D de la ventana de acordes: dado un diagrama (que cuerdas suenan y en que
 * traste), adivina que acorde es. Puede haber mas de un nombre igual de valido -las
 * mismas notas a veces forman mas de un acorde, y ahi entran los nombres alternativos.
 */
public final class ChordNamer {

    private ChordNamer() {
    }

    /** Los acordes que explican ese diagrama, del mas exacto al mas parcial. */
    public static List<Chord> namesFor(ChordDiagram diagram, Tuning tuning) {
        Set<Integer> sounded = new HashSet<>();
        int lowestSoundingString = lowestSoundingString(diagram);
        if (lowestSoundingString < 0) {
            return List.of();
        }
        for (int string = 1; string <= diagram.stringCount(); string++) {
            if (diagram.isPlayed(string)) {
                sounded.add(soundedSemitone(diagram, tuning, string));
            }
        }
        int bassSemitone = soundedSemitone(diagram, tuning, lowestSoundingString);
        PitchClass bass = PitchClass.fromSemitone(bassSemitone);

        List<Chord> matches = new ArrayList<>();
        for (int rootSemitone = 0; rootSemitone < 12; rootSemitone++) {
            PitchClass root = PitchClass.fromSemitone(rootSemitone);
            for (ChordType type : ChordType.values()) {
                Chord candidate = bassSemitone == rootSemitone
                        ? Chord.of(root, type)
                        : Chord.inverted(root, type, bass);
                if (explains(candidate, sounded)) {
                    matches.add(candidate);
                }
            }
        }
        return matches.stream()
                .sorted(Comparator.comparingInt((Chord chord) -> omittedToneCount(chord, sounded)))
                .toList();
    }

    private static boolean explains(Chord candidate, Set<Integer> sounded) {
        return sounded.containsAll(candidate.essentialSemitones()) && candidate.formulaSemitones().containsAll(sounded);
    }

    private static int omittedToneCount(Chord chord, Set<Integer> sounded) {
        return chord.formulaSemitones().size() - sounded.size();
    }

    private static int lowestSoundingString(ChordDiagram diagram) {
        for (int string = diagram.stringCount(); string >= 1; string--) {
            if (diagram.isPlayed(string)) {
                return string;
            }
        }
        return -1;
    }

    private static int soundedSemitone(ChordDiagram diagram, Tuning tuning, int string) {
        return Math.floorMod(tuning.pitchOfString(string).midiNumber() + diagram.fretOfString(string), 12);
    }
}
