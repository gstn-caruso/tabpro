package com.gstncaruso.tabpro.format.exchange;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.NoteValue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class DurationTicks {

    public static final long GRID_TICKS = Duration.of(NoteValue.SIXTY_FOURTH).ticks();

    private static final List<Duration> SIMPLE_DURATIONS = simpleDurations();

    private DurationTicks() {
    }

    public static Duration nearestTo(long ticks) {
        return SIMPLE_DURATIONS.stream()
                .min(Comparator.comparingLong(duration -> Math.abs(duration.ticks() - ticks)))
                .orElseThrow();
    }

    public static Duration nearestTo(long ticks, NoteValue finestGrid) {
        long gridTicks = Duration.of(finestGrid).ticks();
        return SIMPLE_DURATIONS.stream()
                .filter(duration -> duration.ticks() % gridTicks == 0)
                .min(Comparator.comparingLong(duration -> Math.abs(duration.ticks() - ticks)))
                .orElseThrow();
    }

    public static List<Duration> decompose(long ticks) {
        long rounded = Math.round(ticks / (double) GRID_TICKS) * GRID_TICKS;
        if (rounded <= 0) {
            return List.of();
        }
        Optional<Duration> exact = exactMatch(rounded);
        if (exact.isPresent()) {
            return List.of(exact.get());
        }
        return greedyBinaryDecomposition(rounded);
    }

    private static Optional<Duration> exactMatch(long ticks) {
        return SIMPLE_DURATIONS.stream().filter(duration -> duration.ticks() == ticks).findFirst();
    }

    private static List<Duration> greedyBinaryDecomposition(long ticks) {
        List<Duration> result = new ArrayList<>();
        long remaining = ticks;
        for (NoteValue value : NoteValue.values()) {
            long unit = Duration.of(value).ticks();
            while (remaining >= unit) {
                result.add(Duration.of(value));
                remaining -= unit;
            }
        }
        return result;
    }

    private static List<Duration> simpleDurations() {
        List<Duration> durations = new ArrayList<>();
        for (NoteValue value : NoteValue.values()) {
            durations.add(new Duration(value, false));
            durations.add(new Duration(value, true));
        }
        return List.copyOf(durations);
    }
}
