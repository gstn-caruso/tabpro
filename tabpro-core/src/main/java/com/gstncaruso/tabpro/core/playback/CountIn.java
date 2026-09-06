package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.TimeSignature;

/** La cuenta regresiva antes de arrancar: un compas vacio, si esta activada. */
public record CountIn(boolean enabled) {

    private static final CountIn OFF = new CountIn(false);
    private static final CountIn ON = new CountIn(true);

    public static CountIn off() {
        return OFF;
    }

    public static CountIn on() {
        return ON;
    }

    /** Cuantos ticks hay que anteponer a la reproduccion. */
    public long leadInTicks(TimeSignature timeSignature) {
        return enabled ? timeSignature.ticksPerMeasure() : 0;
    }
}
