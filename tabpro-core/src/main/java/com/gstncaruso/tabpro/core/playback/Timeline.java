package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Score;
import java.util.ArrayList;
import java.util.List;

public record Timeline(int tempoBpm, int ticksPerQuarter, List<TrackTimeline> tracks) {

    public static Timeline of(Score score) {
        PlayOrder order = PlayOrder.of(score);
        List<TrackTimeline> trackTimelines = new ArrayList<>();
        for (int index = 0; index < score.trackCount(); index++) {
            boolean audible = score.isAudible(index);
            trackTimelines.add(new TrackRenderer(score.track(index), order, audible).render());
        }
        return new Timeline(score.tempo(), Duration.TICKS_PER_QUARTER, trackTimelines);
    }

    public long endTick() {
        return tracks.stream().mapToLong(TrackTimeline::endTick).max().orElse(0);
    }
}
