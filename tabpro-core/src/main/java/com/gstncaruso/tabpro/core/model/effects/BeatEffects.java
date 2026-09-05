package com.gstncaruso.tabpro.core.model.effects;

import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.util.Optional;

/** Lo que se le pide al beat entero, y no a una nota suelta. */
public record BeatEffects(
        Optional<Stroke> stroke,
        Optional<PickstrokeDirection> pickstroke,
        boolean fadeIn,
        boolean tapping,
        boolean slapping,
        boolean popping,
        boolean wideVibrato,
        Optional<Bend> tremoloBar,
        Optional<Wah> wah,
        Optional<String> text,
        Optional<ChordDiagram> chord) {

    private static final BeatEffects NONE = new BeatEffects(
            Optional.empty(), Optional.empty(), false, false, false, false, false,
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public static BeatEffects none() {
        return NONE;
    }

    public boolean isEmpty() {
        return equals(NONE);
    }

    public BeatEffects withStroke(Stroke stroke) {
        return new BeatEffects(Optional.ofNullable(stroke), pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord);
    }

    public BeatEffects withPickstroke(PickstrokeDirection pickstroke) {
        return new BeatEffects(stroke, Optional.ofNullable(pickstroke), fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord);
    }

    public BeatEffects withFadeIn(boolean fadeIn) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord);
    }

    public BeatEffects withTapping(boolean tapping) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord);
    }

    public BeatEffects withSlapping(boolean slapping) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord);
    }

    public BeatEffects withPopping(boolean popping) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord);
    }

    public BeatEffects withWideVibrato(boolean wideVibrato) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord);
    }

    public BeatEffects withTremoloBar(Bend tremoloBar) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, Optional.ofNullable(tremoloBar), wah, text, chord);
    }

    public BeatEffects withWah(Wah wah) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, Optional.ofNullable(wah), text, chord);
    }

    public BeatEffects withText(String text) {
        Optional<String> written = Optional.ofNullable(text).filter(value -> !value.isBlank());
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, written, chord);
    }

    public BeatEffects withChord(ChordDiagram chord) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, Optional.ofNullable(chord));
    }
}
