package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Score;
import java.util.ArrayList;
import java.util.List;

public record Timeline(int tempoBpm, int ticksPerQuarter, List<TrackTimeline> tracks) {

    public static Timeline of(Score score) {
        return of(score, PlayOrder.of(score));
    }

    /** El mismo armado, pero recorriendo un orden de compases propio: un rango, un loop, una posicion. */
    public static Timeline of(Score score, PlayOrder order) {
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

    /** El mismo timeline empezando mas tarde: para anteponerle una cuenta regresiva. */
    /** La misma musica a otra velocidad, como pide el entrenador de velocidad. */
    public Timeline withTempo(int bpm) {
        return new Timeline(bpm, ticksPerQuarter, tracks);
    }

    public Timeline shiftedBy(long ticks) {
        return new Timeline(tempoBpm, ticksPerQuarter, tracks.stream().map(track -> track.shiftedBy(ticks)).toList());
    }
}
