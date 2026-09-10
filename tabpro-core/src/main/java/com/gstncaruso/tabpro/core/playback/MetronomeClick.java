package com.gstncaruso.tabpro.core.playback;

public record MetronomeClick(long tick, boolean accented, int velocity) {

    public static final int DEFAULT_VELOCITY = 100;

    public MetronomeClick(long tick, boolean accented) {
        this(tick, accented, DEFAULT_VELOCITY);
    }

    public int sound() {
        return accented ? Metronome.ACCENTED_SOUND : Metronome.BEAT_SOUND;
    }
}
