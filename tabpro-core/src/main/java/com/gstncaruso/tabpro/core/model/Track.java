package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.List;

public record Track(String name, Tuning tuning, Channel channel, List<Measure> measures) {

    public static final int GUITAR_PROGRAM = 25;
    public static final int BASS_PROGRAM = 33;

    public Track {
        if (measures.isEmpty()) {
            throw new IllegalArgumentException("una pista necesita al menos un compas");
        }
        measures = List.copyOf(measures);
    }

    public static Track standardGuitar(String name) {
        return new Track(name, Tuning.standard(), Channel.playing(GUITAR_PROGRAM), List.of(emptyMeasure()));
    }

    public static Track standardBass(String name) {
        return new Track(name, Tuning.standardBass(), Channel.playing(BASS_PROGRAM), List.of(emptyMeasure()));
    }

    private static Measure emptyMeasure() {
        return Measure.empty(TimeSignature.fourFour(), Duration.quarter());
    }

    public Measure measure(int index) {
        return measures.get(index);
    }

    public int measureCount() {
        return measures.size();
    }

    public boolean hasNotesIn(int measureIndex) {
        if (measureIndex < 0 || measureIndex >= measureCount()) {
            return false;
        }
        return measure(measureIndex).beats().stream().anyMatch(beat -> !beat.isRest());
    }

    public Track withName(String name) {
        return new Track(name, tuning, channel, measures);
    }

    public Track withChannel(Channel channel) {
        return new Track(name, tuning, channel, measures);
    }

    public Track withMeasure(int index, Measure measure) {
        List<Measure> updated = new ArrayList<>(measures);
        updated.set(index, measure);
        return withMeasures(updated);
    }

    public Track withMeasureInsertedAt(int index, Measure measure) {
        List<Measure> updated = new ArrayList<>(measures);
        updated.add(index, measure);
        return withMeasures(updated);
    }

    public Track withoutMeasureAt(int index) {
        if (measures.size() == 1) {
            Measure empty = Measure.empty(measure(index).timeSignature(), Duration.quarter());
            return withMeasures(List.of(empty));
        }
        List<Measure> updated = new ArrayList<>(measures);
        updated.remove(index);
        return withMeasures(updated);
    }

    private Track withMeasures(List<Measure> updated) {
        return new Track(name, tuning, channel, updated);
    }
}
