package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import com.gstncaruso.tabpro.core.model.effects.BendType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BendCurveEditor {

    private final List<BendPoint> points = new ArrayList<>();

    public BendCurveEditor(List<BendPoint> initial) {
        points.addAll(initial);
    }

    public static BendCurveEditor of(Bend bend) {
        return new BendCurveEditor(bend.points());
    }

    public static BendCurveEditor blank(BendType type, int quarterTones) {
        return new BendCurveEditor(Bend.of(type, quarterTones).points());
    }

    public List<BendPoint> points() {
        return List.copyOf(points);
    }

    public void reset(List<BendPoint> newPoints) {
        points.clear();
        points.addAll(newPoints);
    }

    public void clickAt(int position, int quarterTones) {
        BendPoint exact = pointAt(position, quarterTones);
        if (exact != null) {
            removeIfEnoughAreLeft(exact);
            return;
        }
        BendPoint sameColumn = pointAtPosition(position);
        if (sameColumn != null) {
            points.remove(sameColumn);
        }
        points.add(new BendPoint(position, quarterTones, sameColumn == null ? 0 : sameColumn.vibrato()));
        sort();
    }

    public void addVibratoAt(int position) {
        nearestTo(position).ifPresent(point -> {
            points.remove(point);
            int nextVibrato = (point.vibrato() + 1) % (BendPoint.MAX_VIBRATO + 1);
            points.add(new BendPoint(point.position(), point.quarterTones(), nextVibrato));
            sort();
        });
    }

    public Bend toBend(BendType type) {
        return new Bend(type, points());
    }

    private void removeIfEnoughAreLeft(BendPoint point) {
        if (points.size() > 2) {
            points.remove(point);
        }
    }

    private BendPoint pointAt(int position, int quarterTones) {
        return points.stream()
                .filter(point -> point.position() == position && point.quarterTones() == quarterTones)
                .findFirst()
                .orElse(null);
    }

    private BendPoint pointAtPosition(int position) {
        return points.stream().filter(point -> point.position() == position).findFirst().orElse(null);
    }

    private java.util.Optional<BendPoint> nearestTo(int position) {
        return points.stream().min(Comparator.comparingInt(point -> Math.abs(point.position() - position)));
    }

    private void sort() {
        points.sort(Comparator.comparingInt(BendPoint::position));
    }
}
