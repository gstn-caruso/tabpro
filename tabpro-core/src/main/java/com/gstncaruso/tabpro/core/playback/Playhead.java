package com.gstncaruso.tabpro.core.playback;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public record Playhead(Map<Integer, BeatPosition> byTrack) {

    public Playhead {
        byTrack = Map.copyOf(byTrack);
    }

    public static Playhead silent() {
        return new Playhead(Map.of());
    }

    public boolean isSilent() {
        return byTrack.isEmpty();
    }

    public Playhead advancedTo(BeatPosition position) {
        Map<Integer, BeatPosition> updated = new LinkedHashMap<>(byTrack);
        updated.put(position.track(), position);
        return new Playhead(updated);
    }

    public Optional<BeatPosition> on(int track) {
        return Optional.ofNullable(byTrack.get(track));
    }

    public OptionalInt measure() {
        return byTrack.values().stream().mapToInt(BeatPosition::measure).max();
    }
}
