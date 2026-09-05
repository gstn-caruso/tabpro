package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.Pitch;

/**
 * Posicion de una nota sonante sobre el pentagrama, para una clave dada.
 *
 * <p>step: grado diatonico contado desde la linea inferior del pentagrama. 0 = linea inferior,
 * 1 = el espacio de arriba, 2 = la segunda linea, etc. Los negativos van para abajo. Los pares
 * son lineas, los impares espacios.
 *
 * <p>sharp: si esa nota necesita un sostenido dibujado (siempre sostenidos, nunca bemoles, sin
 * armadura de clave).
 */
public record StaffPosition(int step, boolean sharp) {

    private static final int SOUNDING_TO_WRITTEN_SEMITONES = 12;

    public static StaffPosition of(Pitch soundingPitch, Clef clef) {
        int written = soundingPitch.midiNumber() + SOUNDING_TO_WRITTEN_SEMITONES;
        int octave = written / 12 - 1;
        int pitchClass = written % 12;
        Letter letter = letterFor(pitchClass);
        int absoluteDiatonicIndex = letter.index() + 7 * octave;
        int step = absoluteDiatonicIndex - clef.bottomLineDiatonicIndex();
        return new StaffPosition(step, letter.sharp());
    }

    public int ledgerLinesBelow() {
        return step >= 0 ? 0 : Math.abs(step) / 2;
    }

    public int ledgerLinesAbove() {
        return step <= 8 ? 0 : (step - 8) / 2;
    }

    public boolean isOnLine() {
        return step % 2 == 0;
    }

    private record Letter(int index, boolean sharp) {
    }

    private static Letter letterFor(int pitchClass) {
        return switch (pitchClass) {
            case 0 -> new Letter(0, false); // C
            case 1 -> new Letter(0, true); // C#
            case 2 -> new Letter(1, false); // D
            case 3 -> new Letter(1, true); // D#
            case 4 -> new Letter(2, false); // E
            case 5 -> new Letter(3, false); // F
            case 6 -> new Letter(3, true); // F#
            case 7 -> new Letter(4, false); // G
            case 8 -> new Letter(4, true); // G#
            case 9 -> new Letter(5, false); // A
            case 10 -> new Letter(5, true); // A#
            case 11 -> new Letter(6, false); // B
            default -> throw new IllegalStateException("clase de altura invalida: " + pitchClass);
        };
    }
}
