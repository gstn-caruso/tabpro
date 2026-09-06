package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Tuning;
import java.util.Optional;

/**
 * Ambas claves suenan una octava mas grave de lo escrito: guitarra y bajo transportan
 * +12 semitonos de lo que suena a lo que se escribe.
 */
public enum Clef {
    TREBLE,
    BASS;

    /** Cuanto mas agudo se escribe una nota que lo que suena -ver la clase-. Lo usa
     * {@link StaffPosition#of} para ir de altura sonante a grado, y {@link #pitchAtStep} para
     * la vuelta. */
    static final int WRITTEN_ABOVE_SOUNDING_SEMITONES = 12;

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
     * La altura sonante que se escribe en ese grado -la inversa de {@link #stepOf}-, siempre en
     * su grafia natural: quien pide un grado pide la linea o el espacio, no una nota puntual, y
     * el sostenido comparte grado con su natural (no se podria elegir cual de las dos). Vacio si
     * esa altura cae fuera del rango MIDI.
     */
    public Optional<Pitch> pitchAtStep(int step) {
        int diatonicIndex = step + bottomLineDiatonicIndex();
        int midi = PitchName.natural(diatonicIndex).midiNumber() - WRITTEN_ABOVE_SOUNDING_SEMITONES;
        return midi < 0 || midi > 127 ? Optional.empty() : Optional.of(new Pitch(midi));
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
