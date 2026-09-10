package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.harmony.PitchClass;
import com.gstncaruso.tabpro.core.harmony.Scale;
import com.gstncaruso.tabpro.core.harmony.ScaleTone;
import java.util.List;
import java.util.Optional;

public final class ChosenScale {

    private Scale scale;
    private PitchClass tonic;

    public Optional<Scale> scale() {
        return Optional.ofNullable(scale);
    }

    public Optional<PitchClass> tonic() {
        return Optional.ofNullable(tonic);
    }

    public void choose(PitchClass tonic, Scale scale) {
        this.tonic = tonic;
        this.scale = scale;
    }

    public void forget() {
        scale = null;
        tonic = null;
    }

    public List<Integer> semitonesFromTheTonic() {
        return tones().stream()
                .map(tone -> Math.floorMod(tone.pitchClass().semitone() - tonic.semitone(), 12))
                .distinct()
                .toList();
    }

    public List<ScaleTone> tones() {
        return scale == null || tonic == null ? List.of() : scale.notesFrom(tonic);
    }
}
