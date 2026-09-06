package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Tuplet;
import java.util.ArrayList;
import java.util.List;

/** Agrupa los beats de un compas que comparten el mismo grupo irregular, para dibujar un unico
 * corchete con su numero sobre todo el grupo. */
public final class Tuplets {

    private Tuplets() {
    }

    public static List<TupletGroup> groupsOf(Measure measure) {
        List<Beat> beats = measure.beats();
        List<TupletGroup> groups = new ArrayList<>();
        int start = -1;
        Tuplet current = Tuplet.none();

        for (int i = 0; i < beats.size(); i++) {
            Tuplet tuplet = beats.get(i).duration().tuplet();
            if (start >= 0 && tuplet.equals(current)) {
                continue;
            }
            if (start >= 0) {
                groups.add(new TupletGroup(start, i - 1, current));
                start = -1;
            }
            if (!tuplet.isPlain()) {
                start = i;
                current = tuplet;
            }
        }
        if (start >= 0) {
            groups.add(new TupletGroup(start, beats.size() - 1, current));
        }
        return groups;
    }
}
