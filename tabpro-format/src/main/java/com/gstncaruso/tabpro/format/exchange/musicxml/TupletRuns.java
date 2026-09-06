package com.gstncaruso.tabpro.format.exchange.musicxml;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Tuplet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Donde hay que anotar el corchete de un grupo irregular: al principio y al final de cada
 * corrida seguida de beats con el mismo grupo (Duration.tuplet()). Un beat sin grupo no lleva
 * marca.
 */
final class TupletRuns {

    record Mark(boolean start, boolean stop) {
    }

    private TupletRuns() {
    }

    static Map<Integer, Mark> of(List<Beat> beats) {
        Map<Integer, Mark> marks = new HashMap<>();
        for (int index = 0; index < beats.size(); index++) {
            Tuplet tuplet = beats.get(index).duration().tuplet();
            if (tuplet.isPlain()) {
                continue;
            }
            boolean start = index == 0 || !beats.get(index - 1).duration().tuplet().equals(tuplet);
            boolean stop = index == beats.size() - 1 || !beats.get(index + 1).duration().tuplet().equals(tuplet);
            marks.put(index, new Mark(start, stop));
        }
        return marks;
    }
}
