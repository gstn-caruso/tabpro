package com.gstncaruso.tabpro.core.notation;

import com.gstncaruso.tabpro.core.model.Pitch;

public record StaffPosition(int step, boolean sharp) {

    public static StaffPosition of(Pitch soundingPitch, Clef clef) {
        PitchName written = PitchName.of(soundingPitch.transposed(Clef.WRITTEN_ABOVE_SOUNDING_SEMITONES));
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

    public StaffPosition shiftedBySteps(int steps) {
        return steps == 0 ? this : new StaffPosition(step + steps, sharp);
    }
}
