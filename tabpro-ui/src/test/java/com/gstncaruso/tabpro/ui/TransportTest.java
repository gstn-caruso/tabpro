package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
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

    @Test
    void toggleWhilePlayingStops() {
        transport.toggle();

        transport.toggle();

        assertFalse(transport.isPlaying());
    }

    @Test
    void showsThePlayingBeat() {
        transport.toggle();

        player.emitBeat(new BeatPosition(0, 0, 1));

        assertEquals(Optional.of(new BeatPosition(0, 0, 1)), transport.playingOn(0));
    }

    @Test
    void hidesThePlayingBeatWhenPlaybackFinishes() {
        transport.toggle();
        player.emitBeat(new BeatPosition(0, 0, 1));

        player.emitFinished();

        assertEquals(Optional.empty(), transport.playingOn(0));
    }

    @Test
    void stoppingHidesThePlayingBeat() {
        transport.toggle();
        player.emitBeat(new BeatPosition(0, 0, 1));

        transport.toggle();

        assertEquals(Optional.empty(), transport.playingOn(0));
    }

    @Test
    void deliversPlayerCallbacksThroughTheUiThread() {
        Deque<Runnable> pending = new ArrayDeque<>();
        Transport queuedTransport = new Transport(editor, player, pending::add);
        queuedTransport.toggle();

        player.emitBeat(new BeatPosition(0, 0, 1));

        assertEquals(Optional.empty(), queuedTransport.playingOn(0));

        while (!pending.isEmpty()) {
            pending.poll().run();
        }

        assertEquals(Optional.of(new BeatPosition(0, 0, 1)), queuedTransport.playingOn(0));
    }

    @Test
    void followsEveryTrackAtOnce() {
        transport.toggle();

        player.emitBeat(new BeatPosition(0, 2, 1));
        player.emitBeat(new BeatPosition(1, 2, 0));

        assertEquals(Optional.of(new BeatPosition(0, 2, 1)), transport.playingOn(0));
        assertEquals(Optional.of(new BeatPosition(1, 2, 0)), transport.playingOn(1));
        assertEquals(java.util.OptionalInt.of(2), transport.playhead().measure());
    }

    @Test
    void notifiesListenersOnEveryChange() {
        int[] notifications = {0};
        transport.addListener(() -> notifications[0]++);

        transport.toggle();
        assertEquals(1, notifications[0], "empezar a sonar es un cambio");

        player.emitBeat(new BeatPosition(0, 0, 0));
        assertEquals(2, notifications[0], "cada beat mueve el cursor de reproducción");

        player.emitFinished();
        assertEquals(3, notifications[0], "terminar tambien es un cambio");

        transport.toggle();
        assertEquals(4, notifications[0]);
    }

    @Test
    void theMetronomeAndTheCountDownAreOffUntilOneAsksForThem() {
        assertFalse(transport.isMetronomeOn());
        assertFalse(transport.isCountDownOn());

        transport.toggleMetronome();
        transport.toggleCountDown();

        assertTrue(transport.isMetronomeOn());
        assertTrue(transport.isCountDownOn());
    }

    @Test
    void aLoopKeepsPlayingUntilOneStopsIt() {
        transport.loopOver(new com.gstncaruso.tabpro.core.playback.LoopRange(0, 0), null);

        assertTrue(transport.loop().isPresent());

        transport.stopLooping();

        assertTrue(transport.loop().isEmpty());
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
        public void playNote(Pitch pitch, int program) {
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
