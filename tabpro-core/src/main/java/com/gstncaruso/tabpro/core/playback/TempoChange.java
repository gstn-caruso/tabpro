package com.gstncaruso.tabpro.core.playback;

/**
 * Un tramo de la reproduccion que suena a un tempo parejo: empieza en ese tick
 * y vale hasta que arranca el siguiente.
 */
public record TempoChange(long tick, int bpm) {

    public TempoChange {
        if (tick < 0) {
            throw new IllegalArgumentException("un tramo no puede empezar antes del principio: " + tick);
        }
        if (bpm <= 0) {
            throw new IllegalArgumentException("bpm debe ser > 0: " + bpm);
        }
    }

    TempoChange shiftedBy(long ticks) {
        return new TempoChange(tick + ticks, bpm);
    }

    TempoChange scaledBy(double factor) {
        return new TempoChange(tick, Math.max(1, (int) Math.round(bpm * factor)));
    }
}
