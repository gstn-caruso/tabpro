package com.gstncaruso.tabpro.core.playback;

public record TempoChange(long tick, int bpm) {

    public TempoChange {
        if (tick < 0) {
            throw new IllegalArgumentException("a segment cannot start before the beginning: " + tick);
        }
        if (bpm <= 0) {
            throw new IllegalArgumentException("bpm must be > 0: " + bpm);
        }
    }

    TempoChange shiftedBy(long ticks) {
        return new TempoChange(tick + ticks, bpm);
    }

    TempoChange scaledBy(double factor) {
        return new TempoChange(tick, Math.max(1, (int) Math.round(bpm * factor)));
    }
}
