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
        PitchName written = PitchName.of(soundingPitch.transposed(SOUNDING_TO_WRITTEN_SEMITONES));
        return new StaffPosition(
                written.diatonicIndex() - clef.bottomLineDiatonicIndex(), written.sharp());
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
}
