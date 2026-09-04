package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.List;

public record Measure(TimeSignature timeSignature, List<Beat> beats) {

    public Measure {
        if (beats.isEmpty()) {
            throw new IllegalArgumentException("un compas necesita al menos un beat");
        }
        beats = List.copyOf(beats);
    }

    public static Measure empty(TimeSignature timeSignature, Duration restDuration) {
        return new Measure(timeSignature, List.of(Beat.rest(restDuration)));
    }

    public long durationTicks() {
        return beats.stream().mapToLong(beat -> beat.duration().ticks()).sum();
    }

    public boolean isComplete() {
        return durationTicks() == timeSignature.ticksPerMeasure();
    }

    public Beat beat(int index) {
        return beats.get(index);
    }

    public Measure withBeat(int index, Beat beat) {
        List<Beat> updated = new ArrayList<>(beats);
        updated.set(index, beat);
        return new Measure(timeSignature, updated);
    }

    public Measure withBeatInsertedAt(int index, Beat beat) {
        List<Beat> updated = new ArrayList<>(beats);
        updated.add(index, beat);
        return new Measure(timeSignature, updated);
    }

    public Measure withoutBeatAt(int index) {
        if (beats.size() == 1) {
            return new Measure(timeSignature, List.of(Beat.rest(beat(index).duration())));
        }
        List<Beat> updated = new ArrayList<>(beats);
        updated.remove(index);
        return new Measure(timeSignature, updated);
    }
}
