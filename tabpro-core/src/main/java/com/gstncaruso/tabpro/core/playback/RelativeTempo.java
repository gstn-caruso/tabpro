package com.gstncaruso.tabpro.core.playback;

/**
 * El tempo relativo con que se puede escuchar la partitura sin tocar el
 * tempo guardado en el archivo: de x0.25 a x2.
 */
public record RelativeTempo(double factor) {

    public static final double MIN = 0.25;
    public static final double MAX = 2.0;

    private static final RelativeTempo NORMAL = new RelativeTempo(1.0);

    public RelativeTempo {
        if (factor < MIN || factor > MAX) {
            throw new IllegalArgumentException("el tempo relativo va de " + MIN + " a " + MAX + ": " + factor);
        }
    }

    public static RelativeTempo normal() {
        return NORMAL;
    }

    public int apply(int bpm) {
        return Math.max(1, (int) Math.round(bpm * factor));
    }

    public Timeline applyTo(Timeline timeline) {
        return new Timeline(apply(timeline.tempoBpm()), timeline.ticksPerQuarter(), timeline.tracks());
    }
}
