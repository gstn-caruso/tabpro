package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.effects.Wah;

public record ScheduledWah(long tick, Wah wah) {

    ScheduledWah shiftedBy(long ticks) {
        return new ScheduledWah(tick + ticks, wah);
    }
}
