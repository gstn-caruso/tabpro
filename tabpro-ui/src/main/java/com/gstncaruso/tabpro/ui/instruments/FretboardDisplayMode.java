package com.gstncaruso.tabpro.ui.instruments;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Los modos de vista que ofrece el diapason, tal como los describe el manual: que
 * mostrar ademas de las notas del beat. Cada uno decide sus posiciones secundarias
 * a su manera, sin cascadas de if en quien los usa.
 */
public enum FretboardDisplayMode {
    ONLY_BEAT("Solo el beat") {
        @Override
        public FretMarks marks(BeatLocation location, int fretCount, Optional<Scale> scale) {
            return FretMarks.of(positionsOf(location.beat()), Set.of());
        }
    },
    BEAT_AND_MEASURE("Beat y compas") {
        @Override
        public FretMarks marks(BeatLocation location, int fretCount, Optional<Scale> scale) {
            Set<FretPosition> ofMeasure = new HashSet<>();
            for (Beat beat : location.measureBeats()) {
                ofMeasure.addAll(positionsOf(beat));
            }
            return FretMarks.of(positionsOf(location.beat()), ofMeasure);
        }
    },
    BEAT_AND_CHORD("Beat y diagrama de acorde") {
        @Override
        public FretMarks marks(BeatLocation location, int fretCount, Optional<Scale> scale) {
            Set<FretPosition> ofChord =
                    location.beat().effects().chord().map(FretboardDisplayMode::positionsOf).orElse(Set.of());
            return FretMarks.of(positionsOf(location.beat()), ofChord);
        }
    },
    BEAT_AND_NEXT_BEAT("Beat y beat siguiente") {
        @Override
        public FretMarks marks(BeatLocation location, int fretCount, Optional<Scale> scale) {
            Set<FretPosition> ofNext = location.nextBeat().map(FretboardDisplayMode::positionsOf).orElse(Set.of());
            return FretMarks.of(positionsOf(location.beat()), ofNext);
        }
    },
    BEAT_AND_SCALE("Beat y escala") {
        @Override
        public FretMarks marks(BeatLocation location, int fretCount, Optional<Scale> scale) {
            Set<FretPosition> ofScale = scale
                    .map(chosen -> positionsInScale(chosen, location.track().tuning(), fretCount))
                    .orElse(Set.of());
            return FretMarks.of(positionsOf(location.beat()), ofScale);
        }
    };

    private final String label;

    FretboardDisplayMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** Las posiciones que este modo marca, primarias (el beat) y secundarias (el contexto). */
    public abstract FretMarks marks(BeatLocation location, int fretCount, Optional<Scale> scale);

    private static Set<FretPosition> positionsOf(Beat beat) {
        Set<FretPosition> positions = new HashSet<>();
        for (Note note : beat.notes()) {
            positions.add(new FretPosition(note.string(), note.fret()));
        }
        return positions;
    }

    private static Set<FretPosition> positionsOf(ChordDiagram chord) {
        Set<FretPosition> positions = new HashSet<>();
        for (int string = 1; string <= chord.stringCount(); string++) {
            if (chord.isPlayed(string)) {
                positions.add(new FretPosition(string, chord.fretOfString(string)));
            }
        }
        return positions;
    }

    private static Set<FretPosition> positionsInScale(Scale scale, Tuning tuning, int fretCount) {
        Set<FretPosition> positions = new HashSet<>();
        for (int string = 1; string <= tuning.stringCount(); string++) {
            for (int fret = 0; fret <= fretCount; fret++) {
                if (scale.contains(tuning.pitchOf(new Note(string, fret)).midiNumber())) {
                    positions.add(new FretPosition(string, fret));
                }
            }
        }
        return positions;
    }

    @Override
    public String toString() {
        return label;
    }
}
