package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.effects.SoundParameter;

/** Un valor de la mesa de mezcla que le toca a una pista en un momento dado. */
public record ScheduledParameter(long tick, SoundParameter parameter, int value) {

    ScheduledParameter shiftedBy(long ticks) {
        return new ScheduledParameter(tick + ticks, parameter, value);
    }
}
