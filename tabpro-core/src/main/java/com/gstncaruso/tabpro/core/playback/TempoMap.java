package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Duration;
import java.util.ArrayList;
import java.util.List;

public record TempoMap(List<TempoChange> changes) {

    public TempoMap {
        if (changes.isEmpty()) {
            throw new IllegalArgumentException("a tempo map has to say what speed it starts at");
        }
        if (changes.getFirst().tick() != 0) {
            throw new IllegalArgumentException("the first segment starts at tick 0");
        }
        changes = collapsed(changes);
    }

    public static TempoMap steady(int bpm) {
        return new TempoMap(List.of(new TempoChange(0, bpm)));
    }

    public int initialBpm() {
        return changes.getFirst().bpm();
    }

    public boolean isSteady() {
        return changes.size() == 1;
    }

    public int bpmAt(long tick) {
        int bpm = initialBpm();
        for (TempoChange change : changes) {
            if (change.tick() > tick) {
                return bpm;
            }
            bpm = change.bpm();
        }
        return bpm;
    }

    public double secondsAt(long tick) {
        double seconds = 0;
        long from = 0;
        for (TempoChange change : changes) {
            if (change.tick() >= tick) {
                break;
            }
            seconds += secondsOf(change.tick() - from, bpmAt(from));
            from = change.tick();
        }
        return seconds + secondsOf(tick - from, bpmAt(from));
    }

    public TempoMap changingTo(long tick, int bpm) {
        List<TempoChange> updated = new ArrayList<>(changes);
        updated.removeIf(change -> change.tick() >= tick);
        updated.add(new TempoChange(tick, bpm));
        return new TempoMap(updated);
    }

    public TempoMap scaledBy(double factor) {
        return new TempoMap(changes.stream().map(change -> change.scaledBy(factor)).toList());
    }

    public TempoMap startingAt(int bpm) {
        return scaledBy((double) bpm / initialBpm());
    }

    public TempoMap shiftedBy(long ticks) {
        if (ticks == 0) {
            return this;
        }
        List<TempoChange> moved = new ArrayList<>();
        moved.add(new TempoChange(0, initialBpm()));
        for (TempoChange change : changes.subList(1, changes.size())) {
            moved.add(change.shiftedBy(ticks));
        }
        return new TempoMap(moved);
    }

    private static double secondsOf(long ticks, int bpm) {
        return (double) ticks / Duration.TICKS_PER_QUARTER * 60.0 / bpm;
    }

    private static List<TempoChange> collapsed(List<TempoChange> changes) {
        List<TempoChange> kept = new ArrayList<>();
        for (TempoChange change : changes) {
            if (!kept.isEmpty() && change.tick() < kept.getLast().tick()) {
                throw new IllegalArgumentException("segments go in the order they sound");
            }
            if (!kept.isEmpty() && change.tick() == kept.getLast().tick()) {
                kept.removeLast();
            }
            if (kept.isEmpty() || kept.getLast().bpm() != change.bpm()) {
                kept.add(change);
            }
        }
        return List.copyOf(kept);
    }
}
