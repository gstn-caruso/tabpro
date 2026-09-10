package com.gstncaruso.tabpro.core.model.effects;

public enum SoundParameter {
    PROGRAM(0, 127),
    VOLUME(0, 127),
    PAN(0, 127),
    CHORUS(0, 127),
    REVERB(0, 127),
    PHASER(0, 127),
    TREMOLO(0, 127),
    TEMPO(20, 400);

    private final int minimum;
    private final int maximum;

    SoundParameter(int minimum, int maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public int minimum() {
        return minimum;
    }

    public int maximum() {
        return maximum;
    }

    public boolean isGlobal() {
        return this == TEMPO;
    }
}
