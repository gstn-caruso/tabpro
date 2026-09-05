package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.List;

/** Una linea melodica dentro de un compas. Una voz sin beats es una voz que no se usa. */
public record Voice(List<Beat> beats) {

    private static final Voice UNUSED = new Voice(List.of());

    public Voice {
        beats = List.copyOf(beats);
    }

    public static Voice unused() {
        return UNUSED;
    }

    public static Voice restingFor(Duration duration) {
        return new Voice(List.of(Beat.rest(duration)));
    }

    public boolean isUnused() {
        return beats.isEmpty();
    }

    public int beatCount() {
        return beats.size();
    }

    public Beat beat(int index) {
        return beats.get(index);
    }

    public long durationTicks() {
        return beats.stream().mapToLong(beat -> beat.duration().ticks()).sum();
    }

    public boolean hasNotes() {
        return beats.stream().anyMatch(beat -> !beat.isRest());
    }

    public Voice withBeat(int index, Beat beat) {
        List<Beat> updated = new ArrayList<>(beats);
        updated.set(index, beat);
        return new Voice(updated);
    }

    public Voice withBeatInsertedAt(int index, Beat beat) {
        List<Beat> updated = new ArrayList<>(beats);
        updated.add(index, beat);
        return new Voice(updated);
    }

    public Voice withBeatAppended(Beat beat) {
        return withBeatInsertedAt(beats.size(), beat);
    }

    public Voice withoutBeatAt(int index) {
        if (beats.size() == 1) {
            return restingFor(beat(index).duration());
        }
        List<Beat> updated = new ArrayList<>(beats);
        updated.remove(index);
        return new Voice(updated);
    }

    public Voice mappingBeats(java.util.function.UnaryOperator<Beat> change) {
        return new Voice(beats.stream().map(change).toList());
    }
}
