package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Score;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

public record Timeline(TempoMap tempo, int ticksPerQuarter, List<TrackTimeline> tracks) {

    public Timeline(int tempoBpm, int ticksPerQuarter, List<TrackTimeline> tracks) {
        this(TempoMap.steady(tempoBpm), ticksPerQuarter, tracks);
    }

    public static Timeline of(Score score) {
        return of(score, PlayOrder.of(score));
    }

    public static Timeline of(Score score, PlayOrder order) {
        SoundAutomation automation = SoundAutomation.of(score, order);
        List<TrackTimeline> trackTimelines = new ArrayList<>();
        for (int index = 0; index < score.trackCount(); index++) {
            boolean audible = score.isAudible(index);
            trackTimelines.add(new TrackRenderer(score.track(index), order, audible).render()
                    .with(automation.onTrack(index)));
        }
        return new Timeline(automation.tempo(), Duration.TICKS_PER_QUARTER, trackTimelines);
    }

    public int tempoBpm() {
        return tempo.initialBpm();
    }

    public long endTick() {
        return tracks.stream().mapToLong(TrackTimeline::endTick).max().orElse(0);
    }

    public Timeline withTempo(int bpm) {
        return new Timeline(tempo.startingAt(bpm), ticksPerQuarter, tracks);
    }

    public Timeline shiftedBy(long ticks) {
        return new Timeline(tempo.shiftedBy(ticks), ticksPerQuarter,
                tracks.stream().map(track -> track.shiftedBy(ticks)).toList());
    }

    public OptionalLong tickOf(int measure, int beat) {
        return tracks.stream()
                .flatMap(track -> track.beats().stream())
                .filter(scheduled -> scheduled.measure() == measure && scheduled.beat() == beat)
                .mapToLong(ScheduledBeat::tick)
                .min();
    }
}
