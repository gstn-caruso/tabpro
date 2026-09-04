package com.gstncaruso.tabpro.core.model;

import java.util.ArrayList;
import java.util.List;

public record Track(String name, Tuning tuning, int midiProgram, List<Measure> measures) {

    public Track {
        if (midiProgram < 0 || midiProgram > 127) {
            throw new IllegalArgumentException("midiProgram debe estar entre 0 y 127: " + midiProgram);
        }
        if (measures.isEmpty()) {
            throw new IllegalArgumentException("una pista necesita al menos un compas");
        }
        measures = List.copyOf(measures);
    }

    public static Track standardGuitar(String name) {
        Measure measure = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        return new Track(name, Tuning.standard(), 25, List.of(measure));
    }

    public Measure measure(int index) {
        return measures.get(index);
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
        return new Track(name, tuning, midiProgram, updated);
    }
}
