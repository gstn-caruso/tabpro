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
import java.util.OptionalInt;
import java.util.function.Consumer;

public final class Transport {

    private final Editor editor;
    private final Player player;
    private final Consumer<Runnable> uiThread;
    private final List<Runnable> listeners = new ArrayList<>();

    private Playhead playhead = Playhead.silent();
    private Timeline currentTimeline;
    private OptionalInt currentTempo = OptionalInt.empty();
    private Metronome metronome;
    private CountIn countIn = CountIn.off();
    private RelativeTempo relativeTempo = RelativeTempo.normal();
    private Optional<SpeedTrainer> speedTrainer = Optional.empty();
    private Optional<LoopRange> loop = Optional.empty();
    private int lap;
    private Optional<Runnable> previewFinished = Optional.empty();

    public Transport(Editor editor, Player player, Consumer<Runnable> uiThread) {
        this(editor, player, uiThread, false);
    }

    public Transport(Editor editor, Player player, Consumer<Runnable> uiThread, boolean metronomeEnabled) {
        this.editor = editor;
        this.player = player;
        this.uiThread = uiThread;
        this.metronome = metronomeEnabled ? Metronome.on() : Metronome.off();
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
        currentTempo = OptionalInt.empty();
        previewFinished = Optional.empty();
        notifyListeners();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void seekTo(int measure, int beat) {
        if (!player.isPlaying() || currentTimeline == null) {
            return;
        }
        currentTimeline.tickOf(measure, beat).ifPresent(player::seekTo);
    }

    public Playhead playhead() {
        return playhead;
    }

    public OptionalInt currentTempoBpm() {
        return currentTempo;
    }

    public Optional<BeatPosition> playingOn(int track) {
        return playhead.on(track);
    }

    public boolean isMetronomeOn() {
        return metronome.enabled();
    }

    public void toggleMetronome() {
        metronome = metronome.withEnabled(!metronome.enabled());
        notifyListeners();
    }

    public int metronomeVolume() {
        return metronome.volume();
    }

    public void setMetronomeVolume(int volume) {
        metronome = metronome.withVolume(volume);
        notifyListeners();
    }

    public void setMetronomeEnabled(boolean enabled) {
        metronome = metronome.withEnabled(enabled);
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

    public void stepForward() {
        if (player.isPlaying()) {
            editor.moveToNextMeasure();
            seekTo(editor.cursor().measure(), 0);
            return;
        }
        editor.moveRight();
        playCurrentBeat();
    }

    public void stepBack() {
        if (player.isPlaying()) {
            editor.moveToPreviousMeasure();
            seekTo(editor.cursor().measure(), 0);
            return;
        }
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

    public void preview(Score score) {
        previewTimeline(Timeline.of(score));
    }

    public void previewTimeline(Timeline timeline) {
        stop();
        player.play(timeline, new InternalListener());
    }

    public void previewBars(Score score, int bars, Runnable onFinished) {
        stop();
        PlayOrder order = new PlaybackRange(0, Math.max(0, bars - 1)).asPlayOrder(score);
        currentTimeline = Timeline.of(score, order);
        previewFinished = Optional.ofNullable(onFinished);
        player.play(currentTimeline, new InternalListener());
        notifyListeners();
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    private void playFrom(int measure) {
        Score score = editor.score();
        PlayOrder order = orderFrom(score, measure);
        Timeline timeline = relativeTempo.applyTo(atTheTempoOfThisLap(Timeline.of(score, order)));
        List<MetronomeClick> clicks = metronome.clicksFor(score, order);
        long leadIn = countIn.leadInTicks(score.timeSignatureOf(order.measureAt(0)));
        currentTimeline = timeline.shiftedBy(leadIn);
        player.play(currentTimeline, shifted(clicks, leadIn), new InternalListener());
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
                currentTempo = tempoAt(position);
                notifyListeners();
            });
        }

        private OptionalInt tempoAt(BeatPosition position) {
            if (currentTimeline == null) {
                return currentTempo;
            }
            java.util.OptionalLong tick = currentTimeline.tickOf(position.measure(), position.beat());
            return tick.isPresent()
                    ? OptionalInt.of(currentTimeline.tempo().bpmAt(tick.getAsLong()))
                    : currentTempo;
        }

        @Override
        public void playbackFinished() {
            uiThread.accept(() -> {
                playhead = Playhead.silent();
                currentTempo = OptionalInt.empty();
                if (loop.isPresent()) {
                    lap++;
                    playFrom(loop.get().fromMeasure());
                    return;
                }
                Optional<Runnable> finished = previewFinished;
                previewFinished = Optional.empty();
                notifyListeners();
                finished.ifPresent(Runnable::run);
            });
        }
    }
}
