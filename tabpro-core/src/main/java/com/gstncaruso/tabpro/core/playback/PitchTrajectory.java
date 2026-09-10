package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public record PitchTrajectory(List<Point> points) {

    public record Point(long tick, double semitones) {
    }

    private static final PitchTrajectory FLAT = new PitchTrajectory(List.of(new Point(0, 0.0)));

    private static final long POINT_VIBRATO_PERIOD_TICKS = Duration.TICKS_PER_QUARTER / 2;

    private static final double POINT_VIBRATO_SEMITONES_PER_LEVEL = 0.25;

    public PitchTrajectory {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("a curve needs at least one point");
        }
        List<Point> sorted = new ArrayList<>(points);
        sorted.sort(Comparator.comparingLong(Point::tick));
        points = List.copyOf(sorted);
    }

    public static PitchTrajectory flat() {
        return FLAT;
    }

    public boolean isFlat() {
        return points.stream().allMatch(point -> point.semitones() == 0.0);
    }

    public static PitchTrajectory of(Bend bend, long noteDurationTicks) {
        List<Point> scaled = bend.points().stream()
                .map(point -> new Point(tickOf(point, noteDurationTicks), point.semitones()))
                .toList();
        return new PitchTrajectory(scaled).plus(pointVibratoOf(bend, noteDurationTicks));
    }

    private static long tickOf(BendPoint point, long noteDurationTicks) {
        return Math.round(point.fractionOfTheNote() * noteDurationTicks);
    }

    private static PitchTrajectory pointVibratoOf(Bend bend, long noteDurationTicks) {
        List<BendPoint> points = bend.points();
        List<Point> wobble = new ArrayList<>(List.of(new Point(0, 0.0), new Point(noteDurationTicks, 0.0)));
        for (int index = 0; index < points.size(); index++) {
            BendPoint point = points.get(index);
            if (point.vibrato() == 0) {
                continue;
            }
            long until = index + 1 < points.size()
                    ? tickOf(points.get(index + 1), noteDurationTicks)
                    : noteDurationTicks;
            wobble.addAll(oscillationBetween(
                    tickOf(point, noteDurationTicks), until,
                    point.vibrato() * POINT_VIBRATO_SEMITONES_PER_LEVEL, POINT_VIBRATO_PERIOD_TICKS));
        }
        return new PitchTrajectory(wobble);
    }

    public static PitchTrajectory ramp(long fromTick, double fromSemitones, long toTick, double toSemitones) {
        return new PitchTrajectory(List.of(new Point(fromTick, fromSemitones), new Point(toTick, toSemitones)));
    }

    public static PitchTrajectory vibrato(long durationTicks, double depthSemitones, long periodTicks) {
        return new PitchTrajectory(oscillationBetween(0, durationTicks, depthSemitones, periodTicks));
    }

    private static List<Point> oscillationBetween(long from, long until, double depthSemitones, long periodTicks) {
        List<Point> wobble = new ArrayList<>();
        long quarterPeriod = Math.max(1, periodTicks / 4);
        double[] shape = {0.0, depthSemitones, 0.0, -depthSemitones};
        int step = 0;
        for (long tick = from; tick < until; tick += quarterPeriod) {
            wobble.add(new Point(tick, shape[step % shape.length]));
            step++;
        }
        wobble.add(new Point(until, 0.0));
        return wobble;
    }

    public boolean staysWithin(double maxAbsoluteSemitones) {
        return points.stream().allMatch(point -> Math.abs(point.semitones()) <= maxAbsoluteSemitones);
    }

    public double semitonesAt(long tick) {
        Point before = points.getFirst();
        for (Point point : points) {
            if (point.tick() == tick) {
                return point.semitones();
            }
            if (point.tick() > tick) {
                return interpolate(before, point, tick);
            }
            before = point;
        }
        return points.getLast().semitones();
    }

    private static double interpolate(Point from, Point to, long tick) {
        long span = to.tick() - from.tick();
        if (span == 0) {
            return to.semitones();
        }
        double progress = (tick - from.tick()) / (double) span;
        return from.semitones() + (to.semitones() - from.semitones()) * progress;
    }

    public PitchTrajectory rampingTo(long tick, double semitones, long rampTicks) {
        long from = Math.max(0, tick - Math.max(rampTicks, 1));
        List<Point> updated = new ArrayList<>(points);
        updated.add(new Point(from, semitonesAt(from)));
        updated.add(new Point(tick, semitones));
        return new PitchTrajectory(updated);
    }

    public PitchTrajectory withJumpAt(long tick, double semitones) {
        return rampingTo(tick, semitones, 1);
    }

    public PitchTrajectory plus(PitchTrajectory other) {
        TreeSet<Long> ticks = new TreeSet<>();
        points.forEach(point -> ticks.add(point.tick()));
        other.points().forEach(point -> ticks.add(point.tick()));
        List<Point> combined = ticks.stream()
                .map(tick -> new Point(tick, semitonesAt(tick) + other.semitonesAt(tick)))
                .toList();
        return new PitchTrajectory(combined);
    }
}
