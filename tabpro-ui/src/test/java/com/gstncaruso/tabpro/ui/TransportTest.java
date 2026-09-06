package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.PlaybackRange;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.RelativeTempo;
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
    void previewTimelinePlaysItDirectlyWithoutGoingThroughTheEditorScore() {
        Timeline timeline = new Timeline(90, Duration.TICKS_PER_QUARTER, java.util.List.of());

        transport.previewTimeline(timeline);

        assertEquals(timeline, player.lastTimeline);
    }

    /**
     * El explorador de partituras del manual: "it is possible to set the number of bars to
     * play before jumping to the next file". previewBars es quien de verdad acota cuantos
     * compases suenan; el salto al siguiente archivo lo decide quien lo llama.
     */
    @Test
    void previewBarsPlaysOnlyTheRequestedBars() {
        Score score = twoMeasureScore();

        transport.previewBars(score, 1, () -> { });

        Timeline expected = Timeline.of(score, new PlaybackRange(0, 0).asPlayOrder(score));
        assertEquals(expected, player.lastTimeline);
    }

    @Test
    void previewBarsWithMoreBarsThanTheScoreHasPlaysItWhole() {
        Score score = twoMeasureScore();

        transport.previewBars(score, 99, () -> { });

        Timeline expected = Timeline.of(score, new PlaybackRange(0, 98).asPlayOrder(score));
        assertEquals(expected, player.lastTimeline);
    }

    @Test
    void previewBarsNotifiesWhenThoseBarsFinishNaturally() {
        boolean[] finished = {false};
        transport.previewBars(twoMeasureScore(), 1, () -> finished[0] = true);

        player.emitFinished();

        assertTrue(finished[0]);
    }

    /**
     * Si alguien para la reproduccion a mano antes de que termine, el aviso no tiene que
     * llegar: quien pidio previewBars -el explorador- no debe saltar al siguiente archivo.
     */
    @Test
    void stoppingBeforeItFinishesCancelsTheNotification() {
        boolean[] finished = {false};
        transport.previewBars(twoMeasureScore(), 1, () -> finished[0] = true);

        transport.stop();
        player.emitFinished();

        assertFalse(finished[0]);
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
    void elMetronomoArrancaConUnVolumenPorDefecto() {
        assertEquals(100, transport.metronomeVolume());
    }

    @Test
    void setMetronomeVolumeCambiaElVolumenSinTocarSiEstaEncendido() {
        transport.toggleMetronome();

        transport.setMetronomeVolume(42);

        assertEquals(42, transport.metronomeVolume());
        assertTrue(transport.isMetronomeOn());
    }

    /**
     * Preferencias [F12], linea 2151 del manual: la pestaña General configura el metronomo.
     * MainFrame siembra este estado al arrancar -{@code new Transport(..., preferences
     * .metronomeEnabled())}-, sin que nadie toque Sonido > Metronomo. La prueba no mira si la
     * preferencia "se leyo": construye el Transport tal cual arranca el programa y comprueba
     * que el metronomo ya esta sonando.
     */
    @Test
    void startsWithTheMetronomeOnWhenTheProgramSaysSo() {
        Transport metronomeOnFromTheStart = new Transport(editor, player, Runnable::run, true);

        assertTrue(metronomeOnFromTheStart.isMetronomeOn());
    }

    @Test
    void startsWithTheMetronomeOffByDefault() {
        assertFalse(transport.isMetronomeOn());
    }

    @Test
    void setMetronomeEnabledPrendeYApagaSinTocarElVolumen() {
        transport.setMetronomeVolume(42);

        transport.setMetronomeEnabled(true);

        assertTrue(transport.isMetronomeOn());
        assertEquals(42, transport.metronomeVolume());

        transport.setMetronomeEnabled(false);

        assertFalse(transport.isMetronomeOn());
        assertEquals(42, transport.metronomeVolume());
    }

    @Test
    void aLoopKeepsPlayingUntilOneStopsIt() {
        transport.loopOver(new com.gstncaruso.tabpro.core.playback.LoopRange(0, 0), null);

        assertTrue(transport.loop().isPresent());

        transport.stopLooping();

        assertTrue(transport.loop().isEmpty());
    }

    @Test
    void theRelativeTempoReachesWhatActuallySounds() {
        transport.setRelativeTempo(new RelativeTempo(0.5));

        transport.toggle();

        assertEquals(60, player.lastTimeline.tempoBpm(), "la mitad de 120");
    }

    @Test
    void startingInTheMiddleKeepsTheTempoThatTheEarlierChangesLeft() {
        Editor slowing = new Editor(scoreSlowingDownInTheFirstMeasure());
        slowing.moveTo(1, 0, 1);
        Transport slowingTransport = new Transport(slowing, player, Runnable::run);

        slowingTransport.toggle();

        assertEquals(90, player.lastTimeline.tempoBpm(),
                "arrancar en el medio recupera el tempo que dejó el cambio anterior");
    }

    /**
     * El manual: "durante la reproduccion, el tempo actual se muestra en la barra de titulo".
     * Importa porque el tempo puede cambiar a mitad de partitura -hay un mapa de tempo- y porque
     * el tempo relativo lo escala.
     */
    @Test
    void hasNoTempoBeforePlaying() {
        assertEquals(java.util.OptionalInt.empty(), transport.currentTempoBpm());
    }

    @Test
    void showsTheTempoOfTheFirstBeat() {
        transport.toggle();

        player.emitBeat(new BeatPosition(0, 0, 0));

        assertEquals(java.util.OptionalInt.of(120), transport.currentTempoBpm());
    }

    @Test
    void showsTheNewTempoAfterAMidScoreChange() {
        Editor editorConCambioDeTempo = new Editor(scoreThatSlowsDownOnItsSecondBeat());
        Transport transportConCambioDeTempo = new Transport(editorConCambioDeTempo, player, Runnable::run);
        transportConCambioDeTempo.toggle();

        player.emitBeat(new BeatPosition(0, 0, 0));
        assertEquals(java.util.OptionalInt.of(120), transportConCambioDeTempo.currentTempoBpm());

        player.emitBeat(new BeatPosition(0, 0, 1));
        assertEquals(java.util.OptionalInt.of(90), transportConCambioDeTempo.currentTempoBpm());
    }

    @Test
    void theRelativeTempoScalesWhatTheTitleShows() {
        transport.setRelativeTempo(new RelativeTempo(0.5));
        transport.toggle();

        player.emitBeat(new BeatPosition(0, 0, 0));

        assertEquals(java.util.OptionalInt.of(60), transport.currentTempoBpm());
    }

    @Test
    void hidesTheTempoWhenPlaybackStops() {
        transport.toggle();
        player.emitBeat(new BeatPosition(0, 0, 0));

        transport.toggle();

        assertEquals(java.util.OptionalInt.empty(), transport.currentTempoBpm());
    }

    @Test
    void hidesTheTempoWhenPlaybackFinishes() {
        transport.toggle();
        player.emitBeat(new BeatPosition(0, 0, 0));

        player.emitFinished();

        assertEquals(java.util.OptionalInt.empty(), transport.currentTempoBpm());
    }

    private static Score scoreThatSlowsDownOnItsSecondBeat() {
        Beat first = Beat.of(Duration.quarter(), new Note(1, 0));
        Beat slowsDown = Beat.of(Duration.quarter(), new Note(1, 1)).withEffects(
                BeatEffects.none().withParameterChange(
                        ParameterChange.nothing().changing(SoundParameter.TEMPO, 90)));
        Measure measure = new Measure(TimeSignature.fourFour(), java.util.List.of(first, slowsDown));
        return new Score("", 120, java.util.List.of(Track.standardGuitar("Guitarra").withMeasure(0, measure)));
    }

    /**
     * El manual: durante la reproduccion se puede hacer clic en la partitura para volver a
     * arrancar desde ahi sin frenar. Sin reproduccion no hay nada que saltar.
     */
    @Test
    void seekToDoesNothingWhenNotPlaying() {
        transport.seekTo(0, 0);

        assertEquals(null, player.lastSeekTick);
    }

    @Test
    void clickingDuringPlaybackAsksThePlayerToJumpToThatTick() {
        Editor editorDeDosCompases = new Editor(twoMeasureScore());
        Transport transportDeDosCompases = new Transport(editorDeDosCompases, player, Runnable::run);
        transportDeDosCompases.toggle();

        transportDeDosCompases.seekTo(1, 0);

        assertEquals(Long.valueOf(Duration.quarter().ticks() * 4), player.lastSeekTick);
    }

    @Test
    void seekingToABeatThatDoesNotExistDoesNothing() {
        transport.toggle();

        transport.seekTo(99, 0);

        assertEquals(null, player.lastSeekTick);
    }

    /**
     * El manual: "los botones permiten reproducir la partitura nota por nota. Durante la
     * reproducción, estos botones cambian a ◀◀ ▶▶ y permiten ir al compás anterior o al
     * siguiente sin frenar." Sin reproducción, stepForward/stepBack siguen navegando nota a nota
     * y no le piden nada al player.
     */
    @Test
    void withoutPlaybackStepForwardMovesNoteByNoteAndDoesNotSeek() {
        transport.stepForward();

        assertEquals(null, player.lastSeekTick);
    }

    @Test
    void duringPlaybackStepForwardGoesToTheNextMeasureWithoutStopping() {
        Editor editorDeDosCompases = new Editor(twoMeasureScore());
        Transport transportDeDosCompases = new Transport(editorDeDosCompases, player, Runnable::run);
        transportDeDosCompases.toggle();

        transportDeDosCompases.stepForward();

        assertEquals(1, editorDeDosCompases.cursor().measure(), "tiene que saltar al compas siguiente");
        assertEquals(Long.valueOf(Duration.quarter().ticks() * 4), player.lastSeekTick);
        assertTrue(transportDeDosCompases.isPlaying(), "no se tiene que frenar");
    }

    @Test
    void duringPlaybackStepBackGoesToThePreviousMeasureWithoutStopping() {
        Editor editorDeDosCompases = new Editor(twoMeasureScore());
        Transport transportDeDosCompases = new Transport(editorDeDosCompases, player, Runnable::run);
        transportDeDosCompases.toggle();
        transportDeDosCompases.stepForward();

        transportDeDosCompases.stepBack();

        assertEquals(0, editorDeDosCompases.cursor().measure(), "tiene que saltar al compas anterior");
        assertEquals(Long.valueOf(0), player.lastSeekTick);
        assertTrue(transportDeDosCompases.isPlaying(), "no se tiene que frenar");
    }

    @Test
    void duringPlaybackStepBackAtTheFirstMeasureStaysThereAndStillSeeks() {
        Editor editorDeDosCompases = new Editor(twoMeasureScore());
        Transport transportDeDosCompases = new Transport(editorDeDosCompases, player, Runnable::run);
        transportDeDosCompases.toggle();

        transportDeDosCompases.stepBack();

        assertEquals(0, editorDeDosCompases.cursor().measure());
        assertEquals(Long.valueOf(0), player.lastSeekTick);
    }

    @Test
    void duringPlaybackStepForwardAtTheLastMeasureStaysThereAndStillSeeks() {
        Editor editorDeDosCompases = new Editor(twoMeasureScore());
        Transport transportDeDosCompases = new Transport(editorDeDosCompases, player, Runnable::run);
        transportDeDosCompases.toggle();
        transportDeDosCompases.stepForward();

        transportDeDosCompases.stepForward();

        assertEquals(1, editorDeDosCompases.cursor().measure());
        assertEquals(Long.valueOf(Duration.quarter().ticks() * 4), player.lastSeekTick);
    }

    private static Score twoMeasureScore() {
        Measure first = new Measure(TimeSignature.fourFour(), java.util.List.of(
                Beat.of(Duration.quarter(), new Note(1, 0)),
                Beat.of(Duration.quarter(), new Note(1, 1)),
                Beat.of(Duration.quarter(), new Note(1, 2)),
                Beat.of(Duration.quarter(), new Note(1, 3))));
        Measure second = new Measure(TimeSignature.fourFour(), java.util.List.of(
                Beat.of(Duration.quarter(), new Note(1, 4))));
        return new Score("", 120, java.util.List.of(
                Track.standardGuitar("Guitarra").withMeasures(java.util.List.of(first, second))));
    }

    private static Score scoreSlowingDownInTheFirstMeasure() {
        Beat slowsDown = Beat.of(Duration.quarter(), new Note(1, 0)).withEffects(
                BeatEffects.none().withParameterChange(
                        ParameterChange.nothing().changing(SoundParameter.TEMPO, 90)));
        Measure first = new Measure(TimeSignature.fourFour(), java.util.List.of(slowsDown));
        Measure second = Measure.empty(TimeSignature.fourFour(), Duration.quarter());
        return new Score("", 120, java.util.List.of(
                Track.standardGuitar("Guitarra").withMeasures(java.util.List.of(first, second))));
    }

    private static final class FakePlayer implements Player {

        private Timeline lastTimeline;
        private PlaybackListener listener;
        private boolean playing;
        private Long lastSeekTick;

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
        public void seekTo(long tick) {
            lastSeekTick = tick;
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
