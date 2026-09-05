package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Como se mueve la altura de una nota mientras suena, en semitonos relativos
 * a su altura escrita. Un bend, la palanca, un slide y el vibrato son todos
 * la misma idea: una curva a lo largo del tiempo de la nota.
 */
public record PitchTrajectory(List<Point> points) {

    public record Point(long tick, double semitones) {
    }

    private static final PitchTrajectory FLAT = new PitchTrajectory(List.of(new Point(0, 0.0)));

    public PitchTrajectory {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("una curva necesita al menos un punto");
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

    /** La curva de un bend, escalando sus posiciones (0 a 60) a los ticks reales de la nota. */
    public static PitchTrajectory of(Bend bend, long noteDurationTicks) {
        List<Point> scaled = bend.points().stream()
                .map(point -> new Point(Math.round(point.fractionOfTheNote() * noteDurationTicks), point.semitones()))
                .toList();
        return new PitchTrajectory(scaled);
    }

    /** Una rampa lineal de un valor a otro entre dos ticks. */
    public static PitchTrajectory ramp(long fromTick, double fromSemitones, long toTick, double toSemitones) {
        return new PitchTrajectory(List.of(new Point(fromTick, fromSemitones), new Point(toTick, toSemitones)));
    }

    /** Una vibrada que oscila entre +profundidad y -profundidad cada periodo. */
    public static PitchTrajectory vibrato(long durationTicks, double depthSemitones, long periodTicks) {
        List<Point> wobble = new ArrayList<>();
        long quarterPeriod = Math.max(1, periodTicks / 4);
        double[] shape = {0.0, depthSemitones, 0.0, -depthSemitones};
        int step = 0;
        for (long tick = 0; tick <= durationTicks; tick += quarterPeriod) {
            wobble.add(new Point(tick, shape[step % shape.length]));
            step++;
        }
        if (wobble.isEmpty() || wobble.getLast().tick() < durationTicks) {
            wobble.add(new Point(durationTicks, 0.0));
        }
        return new PitchTrajectory(wobble);
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

    /** Un cambio de altura instantaneo, sin rampa: como el de un ligado. */
    public PitchTrajectory withJumpAt(long tick, double semitones) {
        List<Point> updated = new ArrayList<>(points);
        updated.add(new Point(tick - 1, semitonesAt(tick)));
        updated.add(new Point(tick, semitones));
        return new PitchTrajectory(updated);
    }

    /** La suma de esta curva con otra, punto a punto, en la union de los ticks que definen. */
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
