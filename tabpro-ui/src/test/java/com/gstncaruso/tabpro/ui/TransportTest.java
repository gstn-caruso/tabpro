package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransportTest {

    private final Editor editor = new Editor(Score.blank());
    private final FakePlayer player = new FakePlayer();
    private Transport transport;

    @BeforeEach
    void setUp() {
        transport = new Transport(editor, player, Runnable::run);
    }

    @Test
    void toggleStartsPlaybackFromTheEditorScore() {
        transport.toggle();

        assertEquals(Timeline.of(editor.score()), player.lastTimeline);
    }

    private static final class FakePlayer implements Player {

        private Timeline lastTimeline;
        private PlaybackListener listener;
        private boolean playing;

        @Override
        public void play(Timeline timeline, PlaybackListener listener) {
            this.lastTimeline = timeline;
            this.listener = listener;
            this.playing = true;
        }

        @Override
        public void stop() {
            playing = false;
        }

        @Override
        public boolean isPlaying() {
            return playing;
        }

        void emitBeat(BeatPosition position) {
            listener.beatStarted(position);
        }

        void emitFinished() {
            playing = false;
            listener.playbackFinished();
        }
    }
}
