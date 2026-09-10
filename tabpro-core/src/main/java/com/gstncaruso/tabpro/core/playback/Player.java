package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Pitch;
import java.util.List;

public interface Player {

    void play(Timeline timeline, PlaybackListener listener);

    default void play(Timeline timeline, List<MetronomeClick> clicks, PlaybackListener listener) {
        play(timeline, listener);
    }

    void playNote(Pitch pitch, int program);

    default void playSequence(List<Pitch> pitches, int program) {
        pitches.forEach(pitch -> playNote(pitch, program));
    }

    default void seekTo(long tick) {
    }

    void stop();

    boolean isPlaying();
}
