package com.gstncaruso.tabpro.ui.dialogs.midi;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.util.ArrayList;
import java.util.List;

public final class RecordingPlayer implements Player {

    private final List<Timeline> played = new ArrayList<>();
    private boolean stopped;

    @Override
    public void play(Timeline timeline, PlaybackListener listener) {
        played.add(timeline);
    }

    @Override
    public void playNote(Pitch pitch, int program) {
    }

    @Override
    public void stop() {
        stopped = true;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    public List<Timeline> played() {
        return List.copyOf(played);
    }

    public boolean wasStopped() {
        return stopped;
    }
}
