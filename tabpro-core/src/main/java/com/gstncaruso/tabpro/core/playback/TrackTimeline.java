package com.gstncaruso.tabpro.core.playback;

import java.util.List;

public record TrackTimeline(
        int program, int volume, int pan, boolean percussion,
        List<ScheduledNote> notes, List<ScheduledBeat> beats) {

    public TrackTimeline {
        notes = List.copyOf(notes);
        beats = List.copyOf(beats);
    }

    public TrackTimeline(int program, int volume, int pan, List<ScheduledNote> notes, List<ScheduledBeat> beats) {
        this(program, volume, pan, false, notes, beats);
    }

    long endTick() {
        long noteEnd = notes.stream().mapToLong(note -> note.startTick() + note.durationTicks()).max().orElse(0);
        long beatEnd = beats.stream().mapToLong(ScheduledBeat::tick).max().orElse(0);
        return Math.max(noteEnd, beatEnd);
    }
}
