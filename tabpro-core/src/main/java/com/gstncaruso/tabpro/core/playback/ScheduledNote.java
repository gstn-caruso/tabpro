package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Pitch;

public record ScheduledNote(long startTick, long durationTicks, Pitch pitch) {
}
