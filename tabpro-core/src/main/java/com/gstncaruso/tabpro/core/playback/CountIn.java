package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.TimeSignature;

public record CountIn(boolean enabled) {

    private static final CountIn OFF = new CountIn(false);
    private static final CountIn ON = new CountIn(true);

    public static CountIn off() {
        return OFF;
    }

    public static CountIn on() {
        return ON;
    }

    public long leadInTicks(TimeSignature timeSignature) {
        return enabled ? timeSignature.ticksPerMeasure() : 0;
    }
}
