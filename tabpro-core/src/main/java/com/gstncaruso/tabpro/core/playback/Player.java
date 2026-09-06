package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Pitch;
import java.util.List;

public interface Player {

    void play(Timeline timeline, PlaybackListener listener);

    /** Lo mismo, con el metronomo sonando encima. */
    default void play(Timeline timeline, List<MetronomeClick> clicks, PlaybackListener listener) {
        play(timeline, listener);
    }

    /** Hace sonar una sola nota con ese instrumento, para escuchar lo que se escribe. */
    void playNote(Pitch pitch, int program);

    void stop();

    boolean isPlaying();
}
