package com.gstncaruso.tabpro.core.playback;

/** Un click del metronomo: cuando suena y si es el primer pulso del compas. */
public record MetronomeClick(long tick, boolean accented) {

    public int sound() {
        return accented ? Metronome.ACCENTED_SOUND : Metronome.BEAT_SOUND;
    }
}
