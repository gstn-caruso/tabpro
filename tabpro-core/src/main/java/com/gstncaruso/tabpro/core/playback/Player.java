package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Pitch;

public interface Player {

    void play(Timeline timeline, PlaybackListener listener);

    /** Hace sonar una sola nota con ese instrumento, para escuchar lo que se escribe. */
    void playNote(Pitch pitch, int program);

    void stop();

    boolean isPlaying();
}
