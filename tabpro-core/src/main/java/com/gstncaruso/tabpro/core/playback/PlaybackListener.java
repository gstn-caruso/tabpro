package com.gstncaruso.tabpro.core.playback;

public interface PlaybackListener {

    void beatStarted(BeatPosition position);

    void playbackFinished();
}
