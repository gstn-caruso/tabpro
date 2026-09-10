package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.model.effects.Velocity;

public record ScheduledNote(
        long startTick, long durationTicks, Pitch pitch, Velocity velocity, PitchTrajectory bend, boolean fadeIn) {

    private static final Velocity DEFAULT_VELOCITY = new Velocity(100);

    public ScheduledNote(long startTick, long durationTicks, Pitch pitch) {
        this(startTick, durationTicks, pitch, DEFAULT_VELOCITY, PitchTrajectory.flat(), false);
    }

    public boolean carriesAnEffect() {
        return !bend.isFlat() || fadeIn;
    }

    public ScheduledNote withDurationTicks(long durationTicks) {
        return new ScheduledNote(startTick, durationTicks, pitch, velocity, bend, fadeIn);
    }

    ScheduledNote withStartTick(long startTick) {
        return new ScheduledNote(startTick, durationTicks, pitch, velocity, bend, fadeIn);
    }

    public ScheduledNote withBend(PitchTrajectory bend) {
        return new ScheduledNote(startTick, durationTicks, pitch, velocity, bend, fadeIn);
    }

    ScheduledNote withPitch(Pitch pitch) {
        return new ScheduledNote(startTick, durationTicks, pitch, velocity, bend, fadeIn);
    }

    public long endTick() {
        return startTick + durationTicks;
    }
}
