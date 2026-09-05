package com.gstncaruso.tabpro.core.notation;

public record BeamGroup(int firstBeat, int lastBeat) {

    public BeamGroup {
        if (firstBeat < 0) {
            throw new IllegalArgumentException("firstBeat debe ser >= 0: " + firstBeat);
        }
        if (lastBeat < firstBeat) {
            throw new IllegalArgumentException("lastBeat debe ser >= firstBeat: " + lastBeat);
        }
    }

    public boolean isSingle() {
        return firstBeat == lastBeat;
    }

    public int size() {
        return lastBeat - firstBeat + 1;
    }

    public boolean contains(int beatIndex) {
        return beatIndex >= firstBeat && beatIndex <= lastBeat;
    }
}
