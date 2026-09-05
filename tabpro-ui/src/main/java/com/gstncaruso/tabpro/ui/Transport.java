package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.util.function.Consumer;

public final class Transport {

    private final Editor editor;
    private final Player player;
    private final Consumer<Runnable> uiThread;

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
        player.play(Timeline.of(editor.score()), null);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }
}
