package com.gstncaruso.tabpro.ui.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFiles;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * El manual, sobre el botón de escuchar del explorador: "it is possible to set the number of
 * bars to play before jumping to the next file". BrowserPlayback es quien decide, una vez que
 * Transport avisa que esos compases ya sonaron, si hay que seguir con el próximo archivo de la
 * lista -y con cuál-.
 */
class BrowserPlaybackTest {

    private final Path primero = Path.of("/tmp/primero.tabpro");
    private final Path segundo = Path.of("/tmp/segundo.tabpro");
    private final Score scoreDelPrimero = new Score("primero", 100, List.of(Track.standardGuitar("Guitarra")));
    private final Score scoreDelSegundo = new Score("segundo", 110, List.of(Track.standardGuitar("Guitarra")));
    private final FakeScoreFiles files = new FakeScoreFiles();
    private final FakeSound sound = new FakeSound();
    private final FakeListener listener = new FakeListener();
    private BrowserPlayback playback;

    @BeforeEach
    void setUp() {
        files.scores.put(primero, scoreDelPrimero);
        files.scores.put(segundo, scoreDelSegundo);
        playback = new BrowserPlayback(files, sound, listener);
    }

    @Test
    void listeningToTheFirstFileEndsUpPlayingTheSecondOne() {
        playback.play(List.of(primero, segundo), primero, 4);
        assertEquals(scoreDelPrimero, sound.lastScore);

        sound.finishCurrentFile();

        assertEquals(scoreDelSegundo, sound.lastScore);
        assertEquals(2, sound.playCalls);
    }

    @Test
    void theBarsLimitReachesTheSoundOnEveryFileOfTheChain() {
        playback.play(List.of(primero, segundo), primero, 4);
        assertEquals(4, sound.lastBars);

        sound.finishCurrentFile();

        assertEquals(4, sound.lastBars);
    }

    /** Ultimo archivo de la lista: no hay a donde saltar, asi que ahi se corta. */
    @Test
    void theLastFileInTheListJustStopsWhenItFinishes() {
        playback.play(List.of(primero, segundo), segundo, 4);

        sound.finishCurrentFile();

        assertEquals(1, sound.playCalls, "no hay siguiente archivo");
        assertTrue(listener.chainEnded);
    }

    /** El usuario para a mano: el aviso de Transport ya no puede provocar el salto. */
    @Test
    void stoppingByHandCancelsTheJumpToTheNextFile() {
        playback.play(List.of(primero, segundo), primero, 4);

        playback.stop();
        sound.finishCurrentFile();

        assertEquals(1, sound.playCalls, "parar a mano no deja que salte");
        assertTrue(sound.stopped);
    }

    /**
     * La partitura mas corta que el limite la acota Transport.previewBars, no BrowserPlayback:
     * a este objeto solo le llega el aviso de que termino, y salta igual.
     */
    @Test
    void aScoreShorterThanTheLimitStillJumpsWhenItFinishes() {
        playback.play(List.of(primero, segundo), primero, 400);

        sound.finishCurrentFile();

        assertEquals(scoreDelSegundo, sound.lastScore);
    }

    @Test
    void aFileThatFailsToLoadEndsTheChainInsteadOfBreaking() {
        Path roto = Path.of("/tmp/roto.tabpro");

        playback.play(List.of(roto, segundo), roto, 4);

        assertEquals(0, sound.playCalls);
        assertTrue(listener.chainEnded);
    }

    @Test
    void everyJumpTellsTheListenerWhichFileIsPlayingNow() {
        playback.play(List.of(primero, segundo), primero, 4);
        sound.finishCurrentFile();

        assertEquals(List.of(primero, segundo), listener.advancedTo);
    }

    private static final class FakeScoreFiles implements ScoreFiles {
        private final Map<Path, Score> scores = new HashMap<>();

        @Override
        public Score load(Path path) {
            Score score = scores.get(path);
            if (score == null) {
                throw new ScoreFileException("no existe " + path);
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

        @Override
        public void advancedTo(Path path) {
            advancedTo.add(path);
        }

        @Override
        public void chainEnded() {
            chainEnded = true;
        }
    }
}
