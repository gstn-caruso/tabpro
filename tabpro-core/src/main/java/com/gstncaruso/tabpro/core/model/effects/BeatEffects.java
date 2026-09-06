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
        Optional<ChordDiagram> chord,
        ParameterChange parameterChange,
        BeamBreak beamBreak,
        StemOverride stemOverride) {

    private static final BeatEffects NONE = new BeatEffects(
            Optional.empty(), Optional.empty(), false, false, false, false, false,
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), ParameterChange.nothing(),
            BeamBreak.AUTOMATIC, StemOverride.AUTOMATIC);

    public static BeatEffects none() {
        return NONE;
    }

    public boolean isEmpty() {
        return equals(NONE);
    }

    public BeatEffects withStroke(Stroke stroke) {
        return new BeatEffects(Optional.ofNullable(stroke), pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord, parameterChange, beamBreak, stemOverride);
    }

    public BeatEffects withPickstroke(PickstrokeDirection pickstroke) {
        return new BeatEffects(stroke, Optional.ofNullable(pickstroke), fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord, parameterChange, beamBreak, stemOverride);
    }

    public BeatEffects withFadeIn(boolean fadeIn) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord, parameterChange, beamBreak, stemOverride);
    }

    public BeatEffects withTapping(boolean tapping) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord, parameterChange, beamBreak, stemOverride);
    }

    public BeatEffects withSlapping(boolean slapping) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord, parameterChange, beamBreak, stemOverride);
    }

    public BeatEffects withPopping(boolean popping) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord, parameterChange, beamBreak, stemOverride);
    }

    public BeatEffects withWideVibrato(boolean wideVibrato) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord, parameterChange, beamBreak, stemOverride);
    }

    public BeatEffects withTremoloBar(Bend tremoloBar) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, Optional.ofNullable(tremoloBar), wah, text, chord, parameterChange, beamBreak, stemOverride);
    }

    public BeatEffects withWah(Wah wah) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, Optional.ofNullable(wah), text, chord, parameterChange, beamBreak, stemOverride);
    }

    public BeatEffects withText(String text) {
        Optional<String> written = Optional.ofNullable(text).filter(value -> !value.isBlank());
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, written, chord, parameterChange, beamBreak, stemOverride);
    }

    public BeatEffects withParameterChange(ParameterChange parameterChange) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord,
                parameterChange == null ? ParameterChange.nothing() : parameterChange, beamBreak, stemOverride);
    }

    public BeatEffects withChord(ChordDiagram chord) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, Optional.ofNullable(chord), parameterChange, beamBreak, stemOverride);
    }

    /**
     * "Es posible cambiar a mano las barras... usando el menu Nota" (manual, linea 923). El
     * corte se pide sobre este beat -misma forma que {@link com.gstncaruso.tabpro.core.model.bars.LineBreak}:
     * automatico por default, o forzado a lo que pida el usuario.
     */
    public BeatEffects withBeamBreak(BeamBreak beamBreak) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord, parameterChange,
                beamBreak == null ? BeamBreak.AUTOMATIC : beamBreak, stemOverride);
    }

    /** "...y la direccion de la plica" (manual, linea 923), con la misma forma que el corte de barra. */
    public BeatEffects withStemOverride(StemOverride stemOverride) {
        return new BeatEffects(stroke, pickstroke, fadeIn, tapping, slapping, popping, wideVibrato, tremoloBar, wah, text, chord, parameterChange,
                beamBreak, stemOverride == null ? StemOverride.AUTOMATIC : stemOverride);
    }
}
