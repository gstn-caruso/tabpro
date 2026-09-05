package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;

/**
 * Ambas claves suenan una octava mas grave de lo escrito: guitarra y bajo transportan
 * +12 semitonos de lo que suena a lo que se escribe.
 */
public enum Clef {
    TREBLE,
    BASS;

    private static final int LOW_GUITAR_E_MIDI = 40;

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

    /**
     * Indice diatonico absoluto de la linea inferior del pentagrama para esta clave.
     * TREBLE: mi4 (letra E=2, octava 4) -> 2 + 7*4 = 30.
     * BASS: sol2 (letra G=4, octava 2) -> 4 + 7*2 = 18.
     */
    int bottomLineDiatonicIndex() {
        return switch (this) {
            case TREBLE -> 30;
            case BASS -> 18;
        };
    }
}
