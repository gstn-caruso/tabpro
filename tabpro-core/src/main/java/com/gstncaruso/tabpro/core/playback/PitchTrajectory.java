package com.gstncaruso.tabpro.core.playback;

import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
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

    /** Cada cuanto oscila la vibrada que se le pide a un punto de la curva. */
    private static final long POINT_VIBRATO_PERIOD_TICKS = Duration.TICKS_PER_QUARTER / 2;

    /** Cuanto se aparta la altura por cada nivel de vibrada de un punto: uno, dos o tres. */
    private static final double POINT_VIBRATO_SEMITONES_PER_LEVEL = 0.25;

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

    /**
     * La curva de un bend, escalando sus posiciones (0 a 60) a los ticks reales
     * de la nota. El punto al que se le pidio vibrada la agrega encima de la
     * altura que le toca, hasta donde empieza el punto que le sigue.
     */
    public static PitchTrajectory of(Bend bend, long noteDurationTicks) {
        List<Point> scaled = bend.points().stream()
                .map(point -> new Point(tickOf(point, noteDurationTicks), point.semitones()))
                .toList();
        return new PitchTrajectory(scaled).plus(pointVibratoOf(bend, noteDurationTicks));
    }

    private static long tickOf(BendPoint point, long noteDurationTicks) {
        return Math.round(point.fractionOfTheNote() * noteDurationTicks);
    }

    /** La vibrada que piden los puntos de la curva, sumada a lo largo de los tramos que la llevan. */
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

    /** Una rampa lineal de un valor a otro entre dos ticks. */
    public static PitchTrajectory ramp(long fromTick, double fromSemitones, long toTick, double toSemitones) {
        return new PitchTrajectory(List.of(new Point(fromTick, fromSemitones), new Point(toTick, toSemitones)));
    }

    /** Una vibrada que oscila entre +profundidad y -profundidad cada periodo. */
    public static PitchTrajectory vibrato(long durationTicks, double depthSemitones, long periodTicks) {
        return new PitchTrajectory(oscillationBetween(0, durationTicks, depthSemitones, periodTicks));
    }

    /** La oscilacion entre dos ticks, que arranca y termina centrada para no dejar la altura corrida. */
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

    /** Si ningun punto de la curva se aleja de cero mas que ese limite, en cualquier direccion. */
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

    /** Una rampa que llega a esa altura en ese tick, arrancando rampTicks antes. */
    public PitchTrajectory rampingTo(long tick, double semitones, long rampTicks) {
        long from = Math.max(0, tick - Math.max(rampTicks, 1));
        List<Point> updated = new ArrayList<>(points);
        updated.add(new Point(from, semitonesAt(from)));
        updated.add(new Point(tick, semitones));
        return new PitchTrajectory(updated);
    }

    /** Un cambio de altura instantaneo, sin rampa: como el de un ligado. */
    public PitchTrajectory withJumpAt(long tick, double semitones) {
        return rampingTo(tick, semitones, 1);
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
