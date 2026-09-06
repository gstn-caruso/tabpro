package com.gstncaruso.tabpro.core.playback;

import java.util.List;

public record TrackTimeline(
        int program, int volume, int pan, boolean percussion,
        List<ScheduledNote> notes, List<ScheduledBeat> beats, List<ScheduledParameter> parameters) {

    public TrackTimeline {
        notes = List.copyOf(notes);
        beats = List.copyOf(beats);
        parameters = List.copyOf(parameters);
    }

    public TrackTimeline(
            int program, int volume, int pan, boolean percussion,
            List<ScheduledNote> notes, List<ScheduledBeat> beats) {
        this(program, volume, pan, percussion, notes, beats, List.of());
    }

    public TrackTimeline(int program, int volume, int pan, List<ScheduledNote> notes, List<ScheduledBeat> beats) {
        this(program, volume, pan, false, notes, beats, List.of());
    }

    /** La misma pista con los cambios de parametro que le tocan. */
    TrackTimeline with(List<ScheduledParameter> parameters) {
        return new TrackTimeline(program, volume, pan, percussion, notes, beats, parameters);
    }

    long endTick() {
        long noteEnd = notes.stream().mapToLong(note -> note.startTick() + note.durationTicks()).max().orElse(0);
        long beatEnd = beats.stream().mapToLong(ScheduledBeat::tick).max().orElse(0);
        return Math.max(noteEnd, beatEnd);
    }

    TrackTimeline shiftedBy(long ticks) {
        return new TrackTimeline(program, volume, pan, percussion,
                notes.stream().map(note -> note.withStartTick(note.startTick() + ticks)).toList(),
                beats.stream().map(beat -> beat.shiftedBy(ticks)).toList(),
                parameters.stream().map(parameter -> parameter.shiftedBy(ticks)).toList());
    }
}
