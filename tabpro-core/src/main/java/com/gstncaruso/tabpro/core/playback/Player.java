package com.gstncaruso.tabpro.core.playback;

public interface Player {

    void play(Timeline timeline, PlaybackListener listener);

    void stop();

    boolean isPlaying();
}
