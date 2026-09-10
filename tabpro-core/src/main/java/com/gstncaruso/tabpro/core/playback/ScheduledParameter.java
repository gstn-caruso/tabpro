package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.effects.SoundParameter;

public record ScheduledParameter(long tick, SoundParameter parameter, int value) {

    ScheduledParameter shiftedBy(long ticks) {
        return new ScheduledParameter(tick + ticks, parameter, value);
    }
}
