package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.Optional;

public enum Clef {
    TREBLE,
    BASS;

    static final int WRITTEN_ABOVE_SOUNDING_SEMITONES = 12;

    private static final int LOW_GUITAR_E_MIDI = 40;
    private static final int TREBLE_BOTTOM_LINE_E4_DIATONIC_INDEX = 30;
    private static final int BASS_BOTTOM_LINE_G2_DIATONIC_INDEX = 18;

    public static Clef forTuning(Tuning tuning) {
        int lowestMidi = tuning.strings().stream()
                .mapToInt(Pitch::midiNumber)
                .min()
                .orElseThrow();
        return lowestMidi < LOW_GUITAR_E_MIDI ? BASS : TREBLE;
    }

    public int stepOf(Pitch soundingPitch) {
        return StaffPosition.of(soundingPitch, this).step();
    }

    public Optional<Pitch> pitchAtStep(int step) {
        int diatonicIndex = step + bottomLineDiatonicIndex();
        int midi = PitchName.natural(diatonicIndex).midiNumber() - WRITTEN_ABOVE_SOUNDING_SEMITONES;
        return midi < 0 || midi > 127 ? Optional.empty() : Optional.of(new Pitch(midi));
    }

    int bottomLineDiatonicIndex() {
        return switch (this) {
            case TREBLE -> TREBLE_BOTTOM_LINE_E4_DIATONIC_INDEX;
            case BASS -> BASS_BOTTOM_LINE_G2_DIATONIC_INDEX;
        };
    }
}
