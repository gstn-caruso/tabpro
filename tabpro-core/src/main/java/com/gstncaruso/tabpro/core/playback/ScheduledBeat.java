package com.gstncaruso.tabpro.core.playback;

public record ScheduledBeat(long tick, int measure, int beat) {

    ScheduledBeat shiftedBy(long ticks) {
        return new ScheduledBeat(tick + ticks, measure, beat);
    }
}
