package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.effects.Wah;

/** Como queda el pedal de wah-wah de una pista a partir de un momento dado. */
public record ScheduledWah(long tick, Wah wah) {

    ScheduledWah shiftedBy(long ticks) {
        return new ScheduledWah(tick + ticks, wah);
    }
}
