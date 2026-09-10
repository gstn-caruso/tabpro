package com.gstncaruso.tabpro.core.harmony;

import java.util.ArrayList;
import java.util.List;

public record Scale(String name, List<Integer> semitones, List<Integer> letterSteps) {

    public Scale {
        if (semitones.isEmpty()) {
            throw new IllegalArgumentException("a scale needs at least one note");
        }
        if (semitones.size() != letterSteps.size()) {
            throw new IllegalArgumentException("each semitone needs its own letter");
        }
        semitones = List.copyOf(semitones);
        letterSteps = List.copyOf(letterSteps);
    }

    public int degreeCount() {
        return semitones.size();
    }

    public List<ScaleTone> notesFrom(PitchClass tonic) {
        List<ScaleTone> notes = new ArrayList<>(degreeCount());
        for (int i = 0; i < degreeCount(); i++) {
            int letterStep = letterSteps.get(i);
            int semitoneOffset = semitones.get(i);
            PitchClass note = tonic.steppedBy(letterStep, semitoneOffset);
            Interval interval = Interval.matching(letterStep, semitoneOffset)
                    .orElseThrow(() -> new IllegalStateException(
                            "the scale " + name + " has a degree with no known interval: " + letterStep + "/" + semitoneOffset));
            notes.add(new ScaleTone(note, interval, i + 1));
        }
        return notes;
    }
}
