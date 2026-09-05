package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Channel;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.util.ArrayList;
import java.util.List;

public record Timeline(int tempoBpm, int ticksPerQuarter, List<TrackTimeline> tracks) {

    private static final int SILENT = 0;

    public static Timeline of(Score score) {
        List<TrackTimeline> trackTimelines = new ArrayList<>();
        for (int index = 0; index < score.trackCount(); index++) {
            trackTimelines.add(timelineOf(score.track(index), score.isAudible(index)));
        }
        return new Timeline(score.tempo(), Duration.TICKS_PER_QUARTER, trackTimelines);
    }

    private static TrackTimeline timelineOf(Track track, boolean audible) {
        List<ScheduledNote> notes = new ArrayList<>();
        List<ScheduledBeat> beats = new ArrayList<>();
        long tick = 0;
        for (int m = 0; m < track.measures().size(); m++) {
            Measure measure = track.measure(m);
            for (int b = 0; b < measure.beats().size(); b++) {
                Beat beat = measure.beat(b);
                beats.add(new ScheduledBeat(tick, m, b));
                for (var note : beat.notes()) {
                    notes.add(new ScheduledNote(tick, beat.duration().ticks(), track.tuning().pitchOf(note)));
                }
                tick += beat.duration().ticks();
            }
        }
        Channel channel = track.channel();
        int volume = audible ? channel.volume() : SILENT;
        return new TrackTimeline(channel.program(), volume, channel.pan(), notes, beats);
    }

    public long endTick() {
        return tracks.stream().mapToLong(TrackTimeline::endTick).max().orElse(0);
    }
}
