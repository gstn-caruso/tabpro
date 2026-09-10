package com.gstncaruso.tabpro.ui.browser;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Score;
import java.nio.file.Path;
import java.util.List;

public final class BrowserPlayback {

    public interface Sound {

        void play(Score score, int bars, Runnable onFinished);

        void stop();
    }

    public interface Listener {

        void advancedTo(Path path);

        void loadFailed(Path path);

        void chainEnded();
    }

    private final ScoreFiles files;
    private final Sound sound;
    private final Listener listener;
    private List<Path> queue = List.of();
    private int index = -1;
    private int bars = 1;
    private boolean chaining;

    public BrowserPlayback(ScoreFiles files, Sound sound, Listener listener) {
        this.files = files;
        this.sound = sound;
        this.listener = listener;
    }

    public void play(List<Path> queue, Path from, int bars) {
        this.queue = queue;
        this.index = queue.indexOf(from);
        this.bars = Math.max(1, bars);
        chaining = true;
        playCurrent();
    }

    public void stop() {
        chaining = false;
        sound.stop();
    }

    private void playCurrent() {
        if (index < 0 || index >= queue.size()) {
            endChain();
            return;
        }
        Path path = queue.get(index);
        Score score;
        try {
            score = files.load(path);
        } catch (ScoreFileException e) {
            listener.loadFailed(path);
            endChain();
            return;
        }
        listener.advancedTo(path);
        sound.play(score, bars, this::onCurrentFileFinished);
    }

    private void onCurrentFileFinished() {
        if (!chaining) {
            return;
        }
        index++;
        if (index >= queue.size()) {
            endChain();
            return;
        }
        playCurrent();
    }

    private void endChain() {
        chaining = false;
        listener.chainEnded();
    }
}
