package com.gstncaruso.tabpro.core.playback;

import java.util.List;

public record TrackTimeline(int midiProgram, List<ScheduledNote> notes, List<ScheduledBeat> beats) {

    long endTick() {
        long noteEnd = notes.stream().mapToLong(note -> note.startTick() + note.durationTicks()).max().orElse(0);
        long beatEnd = beats.stream().mapToLong(ScheduledBeat::tick).max().orElse(0);
        return Math.max(noteEnd, beatEnd);
    }
}
