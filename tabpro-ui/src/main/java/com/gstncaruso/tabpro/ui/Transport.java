package com.gstncaruso.tabpro.ui;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.editing.Selection;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.BeatPosition;
import com.gstncaruso.tabpro.core.playback.CountIn;
import com.gstncaruso.tabpro.core.playback.LoopRange;
import com.gstncaruso.tabpro.core.playback.Metronome;
import com.gstncaruso.tabpro.core.playback.MetronomeClick;
import com.gstncaruso.tabpro.core.playback.PlayOrder;
import com.gstncaruso.tabpro.core.playback.PlaybackListener;
import com.gstncaruso.tabpro.core.playback.PlaybackRange;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.core.playback.RelativeTempo;
import com.gstncaruso.tabpro.core.playback.SpeedTrainer;
import com.gstncaruso.tabpro.core.playback.Timeline;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * El transporte: lo que el menu Sonido del manual ofrece para escuchar la
 * partitura, con metronomo, cuenta regresiva, loop, entrenador de velocidad y
 * tempo relativo.
 */
public final class Transport {

    private final Editor editor;
    private final Player player;
    private final Consumer<Runnable> uiThread;
    private final List<Runnable> listeners = new ArrayList<>();

    private Playhead playhead = Playhead.silent();
    private Metronome metronome = Metronome.off();
    private CountIn countIn = CountIn.off();
    private RelativeTempo relativeTempo = RelativeTempo.normal();
    private Optional<SpeedTrainer> speedTrainer = Optional.empty();
    private Optional<LoopRange> loop = Optional.empty();
    private int lap;

    public Transport(Editor editor, Player player, Consumer<Runnable> uiThread) {
        this.editor = editor;
        this.player = player;
        this.uiThread = uiThread;
    }

    public void toggle() {
        if (player.isPlaying()) {
            stop();
            return;
        }
        lap = 0;
        playFrom(editor.cursor().measure());
    }

    public void playFromTheBeginning() {
        stop();
        lap = 0;
        playFrom(0);
    }

    public void stop() {
        player.stop();
        playhead = Playhead.silent();
        notifyListeners();
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

    // ---- lo que el menu Sonido configura ----------------------------------

    public boolean isMetronomeOn() {
        return metronome.enabled();
    }

    public void toggleMetronome() {
        metronome = metronome.enabled() ? Metronome.off() : Metronome.on();
        notifyListeners();
    }

    public boolean isCountDownOn() {
        return countIn.enabled();
    }

    public void toggleCountDown() {
        countIn = countIn.enabled() ? CountIn.off() : CountIn.on();
        notifyListeners();
    }

    public RelativeTempo relativeTempo() {
        return relativeTempo;
    }

    public void setRelativeTempo(RelativeTempo tempo) {
        relativeTempo = tempo;
        notifyListeners();
    }

    public Optional<LoopRange> loop() {
        return loop;
    }

    /** Repite un rango de compases, opcionalmente subiendo el tempo en cada vuelta. */
    public void loopOver(LoopRange range, SpeedTrainer trainer) {
        loop = Optional.of(range);
        speedTrainer = Optional.ofNullable(trainer);
        lap = 0;
        stop();
        playFrom(range.fromMeasure());
    }

    public void stopLooping() {
        loop = Optional.empty();
        speedTrainer = Optional.empty();
        notifyListeners();
    }

    /** El modo paso a paso del manual: mover el cursor y escuchar lo que hay ahi. */
    public void stepForward() {
        editor.moveRight();
        playCurrentBeat();
    }

    public void stepBack() {
        editor.moveLeft();
        playCurrentBeat();
    }

    private void playCurrentBeat() {
        if (player.isPlaying()) {
            return;
        }
        int program = editor.currentTrack().channel().program();
        for (com.gstncaruso.tabpro.core.model.Note note : editor.currentBeat().notes()) {
            player.playNote(editor.currentTrack().pitchOf(note), program);
        }
    }

    /** Escucha una partitura que no es la que se esta editando, como el explorador. */
    public void preview(Score score) {
        stop();
        player.play(Timeline.of(score), new InternalListener());
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    // ---- como se arma lo que suena ----------------------------------------

    private void playFrom(int measure) {
        Score score = editor.score();
        PlayOrder order = orderFrom(score, measure);
        Timeline timeline = relativeTempo.applyTo(atTheTempoOfThisLap(Timeline.of(score, order)));
        List<MetronomeClick> clicks = metronome.clicksFor(score, order);
        long leadIn = countIn.leadInTicks(score.timeSignatureOf(order.measureAt(0)));
        player.play(timeline.shiftedBy(leadIn), shifted(clicks, leadIn), new InternalListener());
        notifyListeners();
    }

    private PlayOrder orderFrom(Score score, int measure) {
        if (loop.isPresent()) {
            return loop.get().asPlayOrder(1);
        }
        return editor.selection()
                .map(selection -> rangeOf(selection).asPlayOrder(score))
                .orElseGet(() -> PlaybackRange.from(measure, score).asPlayOrder(score));
    }

    private static PlaybackRange rangeOf(Selection selection) {
        return new PlaybackRange(selection.fromMeasure(), selection.toMeasure());
    }

    /**
     * En el entrenador de velocidad cada vuelta suena un poco mas rapido. Sin
     * entrenador no hay nada que decir sobre el tempo: el que trae la partitura,
     * con los cambios que le hayan metido en el medio, es el bueno.
     */
    private Timeline atTheTempoOfThisLap(Timeline timeline) {
        return speedTrainer.map(trainer -> timeline.withTempo(trainer.tempoForLap(lap))).orElse(timeline);
    }

    private static List<MetronomeClick> shifted(List<MetronomeClick> clicks, long ticks) {
        if (ticks == 0) {
            return clicks;
        }
        List<MetronomeClick> moved = new ArrayList<>(clicks.size());
        for (MetronomeClick click : clicks) {
            moved.add(new MetronomeClick(click.tick() + ticks, click.accented()));
        }
        return moved;
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
                if (loop.isPresent()) {
                    lap++;
                    playFrom(loop.get().fromMeasure());
                    return;
                }
                notifyListeners();
            });
        }
    }
}
