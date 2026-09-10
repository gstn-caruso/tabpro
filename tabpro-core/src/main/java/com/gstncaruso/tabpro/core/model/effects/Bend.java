package com.gstncaruso.tabpro.core.model.effects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record Bend(BendType type, List<BendPoint> points) {

    public Bend {
        if (points.size() < 2) {
            throw new IllegalArgumentException("a curve needs at least two points");
        }
        List<BendPoint> sorted = new ArrayList<>(points);
        sorted.sort(Comparator.comparingInt(BendPoint::position));
        points = List.copyOf(sorted);
    }

    public static Bend of(BendType type, int quarterTones) {
        return new Bend(type, pointsFor(type, quarterTones));
    }

    private static List<BendPoint> pointsFor(BendType type, int height) {
        int middle = BendPoint.LAST_POSITION / 2;
        int quarter = BendPoint.LAST_POSITION / 4;
        int threeQuarters = quarter * 3;
        return switch (type) {
            case BEND -> List.of(BendPoint.at(0, 0), BendPoint.at(middle, height), BendPoint.at(BendPoint.LAST_POSITION, height));
            case BEND_RELEASE -> List.of(
                    BendPoint.at(0, 0), BendPoint.at(quarter, height),
                    BendPoint.at(middle, height), BendPoint.at(BendPoint.LAST_POSITION, 0));
            case BEND_RELEASE_BEND -> List.of(
                    BendPoint.at(0, 0), BendPoint.at(quarter, height), BendPoint.at(middle, 0),
                    BendPoint.at(threeQuarters, height), BendPoint.at(BendPoint.LAST_POSITION, height));
            case PREBEND -> List.of(BendPoint.at(0, height), BendPoint.at(BendPoint.LAST_POSITION, height));
            case PREBEND_RELEASE -> List.of(
                    BendPoint.at(0, height), BendPoint.at(middle, height), BendPoint.at(BendPoint.LAST_POSITION, 0));
            case DIP -> List.of(BendPoint.at(0, 0), BendPoint.at(middle, -height), BendPoint.at(BendPoint.LAST_POSITION, 0));
            case INVERTED_DIP -> List.of(BendPoint.at(0, 0), BendPoint.at(middle, height), BendPoint.at(BendPoint.LAST_POSITION, 0));
            case DIVE -> List.of(BendPoint.at(0, 0), BendPoint.at(BendPoint.LAST_POSITION, -height));
            case RETURN -> List.of(BendPoint.at(0, 0), BendPoint.at(BendPoint.LAST_POSITION, height));
            case RELEASE_UP -> List.of(BendPoint.at(0, -height), BendPoint.at(BendPoint.LAST_POSITION, 0));
            case RELEASE_DOWN -> List.of(BendPoint.at(0, height), BendPoint.at(BendPoint.LAST_POSITION, 0));
        };
    }

    public double semitonesAt(int position) {
        BendPoint before = points.getFirst();
        for (BendPoint point : points) {
            if (point.position() == position) {
                return point.semitones();
            }
            if (point.position() > position) {
                return interpolate(before, point, position);
            }
            before = point;
        }
        return points.getLast().semitones();
    }

    private static double interpolate(BendPoint from, BendPoint to, int position) {
        int span = to.position() - from.position();
        if (span == 0) {
            return to.semitones();
        }
        double progress = (position - from.position()) / (double) span;
        return from.semitones() + (to.semitones() - from.semitones()) * progress;
    }

    public int peakQuarterTones() {
        return points.stream().mapToInt(BendPoint::quarterTones).max().orElse(0);
    }

    public int farthestQuarterTones() {
        return points.stream()
                .max(Comparator.comparingInt(point -> Math.abs(point.quarterTones())))
                .map(BendPoint::quarterTones)
                .orElse(0);
    }

    public boolean startsBent() {
        return points.getFirst().quarterTones() != 0;
    }
}
