package com.gstncaruso.tabpro.ui.percussion;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.util.ArrayList;
import java.util.List;

/** Un reproductor que en vez de sonar anota las notas sueltas que le pidieron. */
final class RecordingPlayer implements Player {

    private final List<Sounded> sounded = new ArrayList<>();

    @Override
    public void play(Timeline timeline, PlaybackListener listener) {
    }

    @Override
    public void playNote(Pitch pitch, int program) {
        sounded.add(new Sounded(pitch, program));
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    List<Sounded> sounded() {
        return List.copyOf(sounded);
    }

    record Sounded(Pitch pitch, int program) {
    }
}
