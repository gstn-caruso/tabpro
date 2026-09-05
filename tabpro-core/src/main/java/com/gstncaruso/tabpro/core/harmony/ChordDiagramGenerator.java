package com.gstncaruso.tabpro.core.harmony;

import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordComplexity;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lo que hace unica a la herramienta de acordes de Guitar Pro: dado un acorde y CUALQUIER
 * afinacion, encontrar todas las posiciones donde se lo puede tocar, respetando que una
 * mano solo alcanza unos pocos trastes de distancia y que las cuerdas al aire cuentan.
 */
public final class ChordDiagramGenerator {

    /** Cuantos trastes de distancia alcanza una mano comoda, de la mas baja a la mas alta que pisa. */
    public static final int DEFAULT_MAX_SPAN = 4;

    /**
     * Alcanza con recorrer una octava: cualquier forma que exista mas arriba del mastil ya
     * aparecio, transportada, dentro de estos primeros doce trastes (las notas se repiten
     * cada doce trastes en cualquier afinacion).
     */
    private static final int SEARCH_WINDOWS = 12;

    private ChordDiagramGenerator() {
    }

    public static List<ChordDiagram> generate(Chord chord, Tuning tuning) {
        return generate(chord, tuning, DEFAULT_MAX_SPAN, ChordComplexity.COMPLEX);
    }

    public static List<ChordDiagram> generate(Chord chord, Tuning tuning, int maxSpan) {
        return generate(chord, tuning, maxSpan, ChordComplexity.COMPLEX);
    }

    /** Todas las posiciones posibles, ordenadas de mas facil a mas dificil y filtradas por complejidad. */
    public static List<ChordDiagram> generate(Chord chord, Tuning tuning, int maxSpan, ChordComplexity maxComplexity) {
        Set<Integer> formula = chord.formulaSemitones();
        Set<Integer> essential = chord.essentialSemitones();
        int bassSemitone = chord.bass().semitone();
        String name = chord.name();

        List<ChordDiagram> found = new ArrayList<>();
        for (int baseFret = 1; baseFret <= SEARCH_WINDOWS; baseFret++) {
            boolean allowOpen = baseFret == 1;
            int windowEnd = Math.min(baseFret + maxSpan, Tuning.MAX_FRET);
            List<List<Integer>> candidates = candidateFretsPerString(tuning, formula, baseFret, windowEnd, allowOpen);
            int[] combo = new int[tuning.stringCount()];
            search(tuning, candidates, 0, combo, essential, bassSemitone, baseFret, name, found);
        }
        return found.stream()
                .filter(diagram -> maxComplexity.accepts(diagram.complexity()))
                .sorted(Comparator.comparingInt(ChordDiagram::difficultyScore))
                .toList();
    }

    private static List<List<Integer>> candidateFretsPerString(
            Tuning tuning, Set<Integer> formula, int windowStart, int windowEnd, boolean allowOpen) {
        List<List<Integer>> perString = new ArrayList<>();
        for (int string = 1; string <= tuning.stringCount(); string++) {
            List<Integer> options = new ArrayList<>();
            options.add(ChordDiagram.MUTED);
            int stringPitch = tuning.pitchOfString(string).midiNumber();
            if (allowOpen && formula.contains(Math.floorMod(stringPitch, 12))) {
                options.add(0);
            }
            for (int fret = windowStart; fret <= windowEnd; fret++) {
                if (formula.contains(Math.floorMod(stringPitch + fret, 12))) {
                    options.add(fret);
                }
            }
            perString.add(options);
        }
        return perString;
    }

    private static void search(
            Tuning tuning,
            List<List<Integer>> candidates,
            int stringIndex,
            int[] combo,
            Set<Integer> essential,
            int bassSemitone,
            int baseFret,
            String name,
            List<ChordDiagram> results) {
        if (stringIndex == candidates.size()) {
            if (isValidVoicing(tuning, combo, essential, bassSemitone, baseFret)) {
                results.add(toDiagram(name, combo));
            }
            return;
        }
        for (int fret : candidates.get(stringIndex)) {
            combo[stringIndex] = fret;
            search(tuning, candidates, stringIndex + 1, combo, essential, bassSemitone, baseFret, name, results);
        }
    }

    private static boolean isValidVoicing(
            Tuning tuning, int[] combo, Set<Integer> essential, int bassSemitone, int baseFret) {
        int played = 0;
        int lowestFretted = Integer.MAX_VALUE;
        int lowestSoundingString = -1;
        Set<Integer> sounded = new HashSet<>();
        for (int i = 0; i < combo.length; i++) {
            int fret = combo[i];
            if (fret == ChordDiagram.MUTED) {
                continue;
            }
            int string = i + 1;
            played++;
            sounded.add(semitoneOf(tuning, string, fret));
            if (fret > 0) {
                lowestFretted = Math.min(lowestFretted, fret);
            }
            lowestSoundingString = Math.max(lowestSoundingString, string);
        }
        if (played < 2 || !sounded.containsAll(essential)) {
            return false;
        }
        int actualBass = semitoneOf(tuning, lowestSoundingString, combo[lowestSoundingString - 1]);
        if (actualBass != bassSemitone) {
            return false;
        }
        // que cada forma se cuente una sola vez: en la ventana que arranca justo en su traste mas bajo.
        return lowestFretted == Integer.MAX_VALUE ? baseFret == 1 : lowestFretted == baseFret;
    }

    private static int semitoneOf(Tuning tuning, int string, int fret) {
        return Math.floorMod(tuning.pitchOfString(string).midiNumber() + fret, 12);
    }

    private static ChordDiagram toDiagram(String name, int[] combo) {
        List<Integer> frets = new ArrayList<>(combo.length);
        for (int fret : combo) {
            frets.add(fret);
        }
        boolean hasOpenString = frets.stream().anyMatch(fret -> fret == 0);
        int lowestFretted = frets.stream().filter(fret -> fret > 0).mapToInt(Integer::intValue).min().orElse(0);
        int baseFret = !hasOpenString && lowestFretted > 1 ? lowestFretted : 1;
        return new ChordDiagram(name, baseFret, frets, List.of(), true).autoFingered();
    }
}
