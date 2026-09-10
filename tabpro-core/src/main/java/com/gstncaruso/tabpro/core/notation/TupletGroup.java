package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.Tuplet;

public record TupletGroup(int firstBeat, int lastBeat, Tuplet tuplet) {

    public TupletGroup {
        if (firstBeat < 0) {
            throw new IllegalArgumentException("firstBeat must be >= 0: " + firstBeat);
        }
        if (lastBeat < firstBeat) {
            throw new IllegalArgumentException("lastBeat must be >= firstBeat: " + lastBeat);
        }
        if (tuplet.isPlain()) {
            throw new IllegalArgumentException("an irregular group cannot be the plain tuplet");
        }
    }

    public boolean isSingle() {
        return firstBeat == lastBeat;
    }

    public boolean contains(int beatIndex) {
        return beatIndex >= firstBeat && beatIndex <= lastBeat;
    }
}
