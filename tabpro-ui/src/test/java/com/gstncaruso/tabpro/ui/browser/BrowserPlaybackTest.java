package com.gstncaruso.tabpro.ui.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BrowserPlaybackTest {

    private final Path first = Path.of("/tmp/first.tabpro");
    private final Path second = Path.of("/tmp/second.tabpro");
    private final Score firstScore = new Score("first", 100, List.of(Track.standardGuitar("Guitarra")));
    private final Score secondScore = new Score("second", 110, List.of(Track.standardGuitar("Guitarra")));
    private final FakeScoreFiles files = new FakeScoreFiles();
    private final FakeSound sound = new FakeSound();
    private final FakeListener listener = new FakeListener();
    private BrowserPlayback playback;

    @BeforeEach
    void setUp() {
        files.scores.put(first, firstScore);
        files.scores.put(second, secondScore);
        playback = new BrowserPlayback(files, sound, listener);
    }

    @Test
    void listeningToTheFirstFileEndsUpPlayingTheSecondOne() {
        playback.play(List.of(first, second), first, 4);
        assertEquals(firstScore, sound.lastScore);

        sound.finishCurrentFile();

        assertEquals(secondScore, sound.lastScore);
        assertEquals(2, sound.playCalls);
    }

    @Test
    void theBarsLimitReachesTheSoundOnEveryFileOfTheChain() {
        playback.play(List.of(first, second), first, 4);
        assertEquals(4, sound.lastBars);

        sound.finishCurrentFile();

        assertEquals(4, sound.lastBars);
    }

    @Test
    void theLastFileInTheListJustStopsWhenItFinishes() {
        playback.play(List.of(first, second), second, 4);

        sound.finishCurrentFile();

        assertEquals(1, sound.playCalls, "there is no next file");
        assertTrue(listener.chainEnded);
    }

    @Test
    void stoppingByHandCancelsTheJumpToTheNextFile() {
        playback.play(List.of(first, second), first, 4);

        playback.stop();
        sound.finishCurrentFile();

        assertEquals(1, sound.playCalls, "stopping by hand prevents the jump");
        assertTrue(sound.stopped);
    }

    @Test
    void aScoreShorterThanTheLimitStillJumpsWhenItFinishes() {
        playback.play(List.of(first, second), first, 400);

        sound.finishCurrentFile();

        assertEquals(secondScore, sound.lastScore);
    }

    @Test
    void aFileThatFailsToLoadEndsTheChainInsteadOfBreaking() {
        Path broken = Path.of("/tmp/broken.tabpro");

        playback.play(List.of(broken, second), broken, 4);

        assertEquals(0, sound.playCalls);
        assertTrue(listener.chainEnded);
    }

    @Test
    void aFileThatFailsToLoadTellsTheListenerWhichPathFailed() {
        Path broken = Path.of("/tmp/broken.tabpro");

        playback.play(List.of(broken, second), broken, 4);

        assertEquals(List.of(broken), listener.loadFailures);
    }

    @Test
    void everyJumpTellsTheListenerWhichFileIsPlayingNow() {
        playback.play(List.of(first, second), first, 4);
        sound.finishCurrentFile();

        assertEquals(List.of(first, second), listener.advancedTo);
    }

    private static final class FakeScoreFiles implements ScoreFiles {
        private final Map<Path, Score> scores = new HashMap<>();

        @Override
        public Score load(Path path) {
            Score score = scores.get(path);
            if (score == null) {
                throw ScoreFileException.cannotRead(path, new NoSuchFileException(path.toString()));
            }
            return score;
        }

        @Override
        public void save(Score score, Path path) {
        }
    }

    private static final class FakeSound implements BrowserPlayback.Sound {
        private Score lastScore;
        private int lastBars;
        private Runnable onFinished;
        private int playCalls;
        private boolean stopped;

        @Override
        public void play(Score score, int bars, Runnable onFinished) {
            this.lastScore = score;
            this.lastBars = bars;
            this.onFinished = onFinished;
            playCalls++;
        }

        @Override
        public void stop() {
            stopped = true;
        }

        void finishCurrentFile() {
            onFinished.run();
        }
    }

    private static final class FakeListener implements BrowserPlayback.Listener {
        private boolean chainEnded;
        private final List<Path> advancedTo = new ArrayList<>();
        private final List<Path> loadFailures = new ArrayList<>();

        @Override
        public void advancedTo(Path path) {
            advancedTo.add(path);
        }

        @Override
        public void loadFailed(Path path) {
            loadFailures.add(path);
        }

        @Override
        public void chainEnded() {
            chainEnded = true;
        }
    }
}
