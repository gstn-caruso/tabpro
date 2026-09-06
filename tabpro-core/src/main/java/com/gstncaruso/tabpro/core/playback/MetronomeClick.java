package com.gstncaruso.tabpro.core.playback;

/** Un click del metronomo: cuando suena, si es el primer pulso del compas y con que fuerza. */
public record MetronomeClick(long tick, boolean accented, int velocity) {

    /** La fuerza con la que suena un click cuando nadie le pidio ninguna en particular. */
    public static final int DEFAULT_VELOCITY = 100;

    public MetronomeClick(long tick, boolean accented) {
        this(tick, accented, DEFAULT_VELOCITY);
    }

    public int sound() {
        return accented ? Metronome.ACCENTED_SOUND : Metronome.BEAT_SOUND;
    }
}
