package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.Tuplet;

/** Un tramo de beats consecutivos que comparten el mismo grupo irregular. */
public record TupletGroup(int firstBeat, int lastBeat, Tuplet tuplet) {

    public TupletGroup {
        if (firstBeat < 0) {
            throw new IllegalArgumentException("firstBeat debe ser >= 0: " + firstBeat);
        }
        if (lastBeat < firstBeat) {
            throw new IllegalArgumentException("lastBeat debe ser >= firstBeat: " + lastBeat);
        }
        if (tuplet.isPlain()) {
            throw new IllegalArgumentException("un grupo irregular no puede ser el tuplet neutro");
        }
    }

    public boolean isSingle() {
        return firstBeat == lastBeat;
    }

    public boolean contains(int beatIndex) {
        return beatIndex >= firstBeat && beatIndex <= lastBeat;
    }
}
