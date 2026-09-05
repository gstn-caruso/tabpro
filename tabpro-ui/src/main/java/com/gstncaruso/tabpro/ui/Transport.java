package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class Transport {

    private final Editor editor;
    private final Player player;
    private final Consumer<Runnable> uiThread;
    private final List<Runnable> listeners = new ArrayList<>();
    private Playhead playhead = Playhead.silent();

    public Transport(Editor editor, Player player, Consumer<Runnable> uiThread) {
        this.editor = editor;
        this.player = player;
        this.uiThread = uiThread;
    }

    /** Escucha una partitura que no es la que se esta editando, como el explorador. */
    public void preview(com.gstncaruso.tabpro.core.model.Score score) {
        if (player.isPlaying()) {
            player.stop();
        }
        player.play(Timeline.of(score), new InternalListener());
    }

    public void toggle() {
        if (player.isPlaying()) {
            player.stop();
            playhead = Playhead.silent();
            notifyListeners();
            return;
        }
        player.play(Timeline.of(editor.score()), new InternalListener());
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public Playhead playhead() {
        return playhead;
    }

    public Optional<BeatPosition> playingOn(int track) {
        return playhead.on(track);
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    private final class InternalListener implements PlaybackListener {

        @Override
        public void beatStarted(BeatPosition position) {
            uiThread.accept(() -> {
                playhead = playhead.advancedTo(position);
                notifyListeners();
            });
        }

        @Override
        public void playbackFinished() {
            uiThread.accept(() -> {
                playhead = Playhead.silent();
                notifyListeners();
            });
        }
    }
}
