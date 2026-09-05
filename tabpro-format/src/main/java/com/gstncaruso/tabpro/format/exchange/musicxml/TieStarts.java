package com.gstncaruso.tabpro.format.exchange.musicxml;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Note;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Donde empieza cada ligadura de una voz. El modelo ya marca donde termina (Note.tied()); esta
 * clase encuentra, para cada nota atada, la nota anterior de la misma cuerda de la que viene la
 * ligadura -- saltando los silencios, que no pertenecen a ninguna cuerda.
 */
final class TieStarts {

    private TieStarts() {
    }

    /** Por indice de beat, las cuerdas que ahi empiezan una ligadura que sigue en un beat posterior. */
    static Map<Integer, Set<Integer>> of(List<Beat> beats) {
        Map<Integer, Integer> lastBeatOnString = new HashMap<>();
        Map<Integer, Set<Integer>> starts = new HashMap<>();
        for (int index = 0; index < beats.size(); index++) {
            for (Note note : beats.get(index).notes()) {
                int string = note.string();
                if (note.tied() && lastBeatOnString.containsKey(string)) {
                    starts.computeIfAbsent(lastBeatOnString.get(string), key -> new HashSet<>()).add(string);
                }
                lastBeatOnString.put(string, index);
            }
        }
        return starts;
    }
}
