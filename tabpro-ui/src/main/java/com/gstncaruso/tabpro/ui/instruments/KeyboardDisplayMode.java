package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Los modos de vista del teclado. Son los mismos cuatro del diapason menos el del
 * diagrama de acorde, que no tiene sentido sobre teclas.
 */
public enum KeyboardDisplayMode {
    ONLY_BEAT("Solo el beat") {
        @Override
        public KeyMarks marks(BeatLocation location, Optional<Scale> scale) {
            Tuning tuning = location.track().tuning();
            return KeyMarks.of(keysOf(location.beat(), tuning), Set.of());
        }
    },
    BEAT_AND_MEASURE("Beat y compas") {
        @Override
        public KeyMarks marks(BeatLocation location, Optional<Scale> scale) {
            Tuning tuning = location.track().tuning();
            Set<Integer> ofMeasure = new HashSet<>();
            for (Beat beat : location.measureBeats()) {
                ofMeasure.addAll(keysOf(beat, tuning));
            }
            return KeyMarks.of(keysOf(location.beat(), tuning), ofMeasure);
        }
    },
    BEAT_AND_NEXT_BEAT("Beat y beat siguiente") {
        @Override
        public KeyMarks marks(BeatLocation location, Optional<Scale> scale) {
            Tuning tuning = location.track().tuning();
            Set<Integer> ofNext = location.nextBeat().map(beat -> keysOf(beat, tuning)).orElse(Set.of());
            return KeyMarks.of(keysOf(location.beat(), tuning), ofNext);
        }
    },
    BEAT_AND_SCALE("Beat y escala") {
        @Override
        public KeyMarks marks(BeatLocation location, Optional<Scale> scale) {
            Tuning tuning = location.track().tuning();
            Set<Integer> ofScale = scale.map(KeyboardDisplayMode::keysInScale).orElse(Set.of());
            return KeyMarks.of(keysOf(location.beat(), tuning), ofScale);
        }
    };

    private final String label;

    KeyboardDisplayMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public abstract KeyMarks marks(BeatLocation location, Optional<Scale> scale);

    private static Set<Integer> keysOf(Beat beat, Tuning tuning) {
        Set<Integer> keys = new HashSet<>();
        for (Note note : beat.notes()) {
            if (note.string() <= tuning.stringCount()) {
                keys.add(tuning.pitchOf(note).midiNumber());
            }
        }
        return keys;
    }

    private static Set<Integer> keysInScale(Scale scale) {
        Set<Integer> keys = new HashSet<>();
        for (int midi = KeyboardView.LOWEST; midi <= KeyboardView.HIGHEST; midi++) {
            if (scale.contains(midi)) {
                keys.add(midi);
            }
        }
        return keys;
    }

    @Override
    public String toString() {
        return label;
    }
}
