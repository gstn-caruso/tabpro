package com.gstncaruso.tabpro.core.playback;

import java.util.List;

public record TrackTimeline(
        int program, int volume, int pan, boolean percussion, int port,
        List<ScheduledNote> notes, List<ScheduledBeat> beats, List<ScheduledParameter> parameters) {

    /** El puerto de salida de una pista que todavia no eligio uno: el primero, tal como usa una pista nueva. */
    private static final int DEFAULT_PORT = 1;

    public TrackTimeline {
        notes = List.copyOf(notes);
        beats = List.copyOf(beats);
        parameters = List.copyOf(parameters);
    }

    public TrackTimeline(
            int program, int volume, int pan, boolean percussion,
            List<ScheduledNote> notes, List<ScheduledBeat> beats, List<ScheduledParameter> parameters) {
        this(program, volume, pan, percussion, DEFAULT_PORT, notes, beats, parameters);
    }

    public TrackTimeline(
            int program, int volume, int pan, boolean percussion,
            List<ScheduledNote> notes, List<ScheduledBeat> beats) {
        this(program, volume, pan, percussion, DEFAULT_PORT, notes, beats, List.of());
    }

    public TrackTimeline(int program, int volume, int pan, List<ScheduledNote> notes, List<ScheduledBeat> beats) {
        this(program, volume, pan, false, DEFAULT_PORT, notes, beats, List.of());
    }

    /** La misma pista con los cambios de parametro que le tocan. */
    TrackTimeline with(List<ScheduledParameter> parameters) {
        return new TrackTimeline(program, volume, pan, percussion, port, notes, beats, parameters);
    }

    long endTick() {
        long noteEnd = notes.stream().mapToLong(note -> note.startTick() + note.durationTicks()).max().orElse(0);
        long beatEnd = beats.stream().mapToLong(ScheduledBeat::tick).max().orElse(0);
        return Math.max(noteEnd, beatEnd);
    }

    TrackTimeline shiftedBy(long ticks) {
        return new TrackTimeline(program, volume, pan, percussion, port,
                notes.stream().map(note -> note.withStartTick(note.startTick() + ticks)).toList(),
                beats.stream().map(beat -> beat.shiftedBy(ticks)).toList(),
                parameters.stream().map(parameter -> parameter.shiftedBy(ticks)).toList());
    }
}
