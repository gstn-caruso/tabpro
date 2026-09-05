package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.util.Optional;
import java.util.function.Consumer;

public final class Transport {

    private final Editor editor;
    private final Player player;
    private final Consumer<Runnable> uiThread;
    private Optional<BeatPosition> playing = Optional.empty();

    public Transport(Editor editor, Player player, Consumer<Runnable> uiThread) {
        this.editor = editor;
        this.player = player;
        this.uiThread = uiThread;
    }

    public void toggle() {
        if (player.isPlaying()) {
            player.stop();
            return;
        }
        player.play(Timeline.of(editor.score()), new InternalListener());
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public Optional<BeatPosition> playing() {
        return playing;
    }

    private final class InternalListener implements PlaybackListener {

        @Override
        public void beatStarted(BeatPosition position) {
            uiThread.accept(() -> playing = Optional.of(position));
        }

        @Override
        public void playbackFinished() {
            uiThread.accept(() -> playing = Optional.empty());
        }
    }
}
