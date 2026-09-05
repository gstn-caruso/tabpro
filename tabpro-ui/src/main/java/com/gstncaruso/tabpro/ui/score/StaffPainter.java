package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.notation.BeamGroup;
import com.gstncaruso.tabpro.core.notation.Beaming;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/** El pentagrama de una pista: clave, figuras, plicas, barras de union y silencios. */
final class StaffPainter {

    private static final double SPACE = ScoreLayout.STAFF_LINE_SPACING;
    private static final double HALF_SPACE = SPACE / 2;
    private static final double NOTE_WIDTH = SPACE * 1.28;
    private static final double NOTE_HEIGHT = SPACE * 0.92;
    private static final double NOTE_TILT = Math.toRadians(-20);
    private static final double STEM_LENGTH = SPACE * 3.4;
    private static final double BEAM_THICKNESS = SPACE * 0.52;
    private static final double BEAM_GAP = SPACE * 0.84;
    private static final int MIDDLE_LINE_STEP = 4;

    private static final BasicStroke THIN = new BasicStroke(1f);
    private static final BasicStroke STEM = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke CLEF = new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private StaffPainter() {
    }

    static void paintStaffLines(Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex) {
        int left = layout.measureX(measureIndex);
        int right = left + layout.measureWidth(measureIndex);

        g.setStroke(THIN);
        g.setColor(ScoreColors.STAFF_LINE);
        for (int line = 0; line <= 4; line++) {
            int y = layout.staffLineY(trackIndex, measureIndex, line);
            g.drawLine(left, y, right, y);
        }

        g.setColor(ScoreColors.BAR_LINE);
        int top = layout.staffTop(trackIndex, measureIndex);
        int bottom = layout.staffBottom(trackIndex, measureIndex);
        g.drawLine(left, top, left, bottom);
        g.drawLine(right, top, right, bottom);
    }

    static void paintClef(Graphics2D g, ScoreLayout layout, Clef clef, int trackIndex, int measureIndex) {
        double x = layout.measureX(measureIndex) + 4.0;
        g.setColor(ScoreColors.INK);
        g.setStroke(CLEF);
        if (clef == Clef.TREBLE) {
            g.draw(trebleClef(x, layout.staffLineY(trackIndex, measureIndex, 1)));
            return;
        }
        double fLineY = layout.staffLineY(trackIndex, measureIndex, 3);
        g.draw(bassClef(x, fLineY));
        double dotX = x + 1.85 * SPACE;
        fill(g, dot(dotX, fLineY - HALF_SPACE, SPACE * 0.17));
        fill(g, dot(dotX, fLineY + HALF_SPACE, SPACE * 0.17));
    }

    static void paintTimeSignature(
            Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex, double x) {
        Measure measure = track.measure(measureIndex);
        g.setColor(ScoreColors.INK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (int) Math.round(SPACE * 2.1)));
        FontMetrics metrics = g.getFontMetrics();

        String top = String.valueOf(measure.timeSignature().beats());
        String bottom = String.valueOf(measure.timeSignature().beatUnit());
        int centerX = (int) Math.round(x + Math.max(metrics.stringWidth(top), metrics.stringWidth(bottom)) / 2.0);
        int upperY = layout.staffLineY(trackIndex, measureIndex, 3) + metrics.getAscent() / 2 - 1;
        int lowerY = layout.staffLineY(trackIndex, measureIndex, 1) + metrics.getAscent() / 2 - 1;
        g.drawString(top, centerX - metrics.stringWidth(top) / 2, upperY);
        g.drawString(bottom, centerX - metrics.stringWidth(bottom) / 2, lowerY);
    }

    static void paintMeasure(
            Graphics2D g, ScoreLayout layout, Track track, Clef clef, int trackIndex, int measureIndex) {
        Measure measure = track.measure(measureIndex);
        List<BeamGroup> groups = Beaming.groupsOf(measure);

        for (int beatIndex = 0; beatIndex < measure.beats().size(); beatIndex++) {
            Beat beat = measure.beat(beatIndex);
            if (beat.isRest()) {
                paintRest(g, layout, trackIndex, measureIndex, beatIndex, beat);
            } else {
                paintNoteheads(g, layout, track, clef, trackIndex, measureIndex, beatIndex, beat);
            }
        }
        for (BeamGroup group : groups) {
            paintBeamGroup(g, layout, track, clef, trackIndex, measureIndex, group);
        }
        paintUnbeamedStems(g, layout, track, clef, trackIndex, measureIndex, groups);
    }

    private static void paintNoteheads(
            Graphics2D g,
            ScoreLayout layout,
            Track track,
            Clef clef,
            int trackIndex,
            int measureIndex,
            int beatIndex,
            Beat beat) {
        double centerX = noteCenterX(layout, trackIndex, measureIndex, beatIndex);
        boolean hollow = beat.duration().value() == NoteValue.WHOLE
                || beat.duration().value() == NoteValue.HALF;

        for (Note note : beat.notes()) {
            StaffPosition position = positionOf(track, clef, note);
            double y = layout.stepY(trackIndex, measureIndex, position.step());
            paintLedgerLines(g, layout, trackIndex, measureIndex, position, centerX);
            if (position.sharp()) {
                paintSharp(g, centerX - NOTE_WIDTH * 0.75 - SPACE * 0.55, y);
            }
            paintNotehead(g, centerX, y, hollow);
            if (beat.duration().dotted()) {
                paintDot(g, layout, trackIndex, measureIndex, position, centerX);
            }
        }
    }

    private static void paintNotehead(Graphics2D g, double centerX, double y, boolean hollow) {
        Shape head = tiltedNotehead(centerX, y);
        g.setColor(ScoreColors.INK);
        if (hollow) {
            g.setStroke(new BasicStroke(1.6f));
            g.draw(head);
            return;
        }
        g.fill(head);
    }

    private static Shape tiltedNotehead(double centerX, double y) {
        Ellipse2D head = new Ellipse2D.Double(
                centerX - NOTE_WIDTH / 2, y - NOTE_HEIGHT / 2, NOTE_WIDTH, NOTE_HEIGHT);
        return AffineTransform.getRotateInstance(NOTE_TILT, centerX, y).createTransformedShape(head);
    }

    private static void paintLedgerLines(
            Graphics2D g,
            ScoreLayout layout,
            int trackIndex,
            int measureIndex,
            StaffPosition position,
            double centerX) {
        g.setColor(ScoreColors.INK);
        g.setStroke(new BasicStroke(1.3f));
        double half = NOTE_WIDTH * 0.80;
        for (int line = 1; line <= position.ledgerLinesBelow(); line++) {
            int y = layout.stepY(trackIndex, measureIndex, -2 * line);
            g.drawLine((int) (centerX - half), y, (int) (centerX + half), y);
        }
        for (int line = 1; line <= position.ledgerLinesAbove(); line++) {
            int y = layout.stepY(trackIndex, measureIndex, 8 + 2 * line);
            g.drawLine((int) (centerX - half), y, (int) (centerX + half), y);
        }
    }

    private static void paintDot(
            Graphics2D g,
            ScoreLayout layout,
            int trackIndex,
            int measureIndex,
            StaffPosition position,
            double centerX) {
        int step = position.isOnLine() ? position.step() + 1 : position.step();
        double y = layout.stepY(trackIndex, measureIndex, step);
        g.setColor(ScoreColors.INK);
        fill(g, dot(centerX + NOTE_WIDTH * 0.85, y, SPACE * 0.17));
    }

    private static void paintSharp(Graphics2D g, double x, double y) {
        g.setColor(ScoreColors.INK);
        g.setStroke(new BasicStroke(1.2f));
        Path2D sharp = new Path2D.Double();
        sharp.moveTo(x + SPACE * 0.18, y - SPACE * 0.85);
        sharp.lineTo(x + SPACE * 0.18, y + SPACE * 0.75);
        sharp.moveTo(x + SPACE * 0.52, y - SPACE * 0.95);
        sharp.lineTo(x + SPACE * 0.52, y + SPACE * 0.65);
        g.draw(sharp);
        g.setStroke(new BasicStroke(1.9f));
        Path2D bars = new Path2D.Double();
        bars.moveTo(x, y - SPACE * 0.16);
        bars.lineTo(x + SPACE * 0.72, y - SPACE * 0.38);
        bars.moveTo(x, y + SPACE * 0.44);
        bars.lineTo(x + SPACE * 0.72, y + SPACE * 0.22);
        g.draw(bars);
    }

    private static void paintUnbeamedStems(
            Graphics2D g,
            ScoreLayout layout,
            Track track,
            Clef clef,
            int trackIndex,
            int measureIndex,
            List<BeamGroup> groups) {
        Measure measure = track.measure(measureIndex);
        for (int beatIndex = 0; beatIndex < measure.beats().size(); beatIndex++) {
            Beat beat = measure.beat(beatIndex);
            if (beat.isRest() || beat.duration().value() == NoteValue.WHOLE || inABeam(groups, beatIndex)) {
                continue;
            }
            Stem stem = stemOf(layout, track, clef, trackIndex, measureIndex, beatIndex, beat);
            g.setColor(ScoreColors.INK);
            g.setStroke(STEM);
            g.draw(new java.awt.geom.Line2D.Double(stem.x(), stem.rootY(), stem.x(), stem.endY()));
            paintFlags(g, stem, Beaming.beamCount(beat.duration().value()));
        }
    }

    private static boolean inABeam(List<BeamGroup> groups, int beatIndex) {
        return groups.stream().anyMatch(group -> !group.isSingle() && group.contains(beatIndex));
    }

    private static void paintFlags(Graphics2D g, Stem stem, int flags) {
        if (flags == 0) {
            return;
        }
        double direction = stem.up() ? 1 : -1;
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int flag = 0; flag < flags; flag++) {
            double y = stem.endY() + direction * flag * BEAM_GAP;
            Path2D hook = new Path2D.Double();
            hook.moveTo(stem.x(), y);
            hook.curveTo(
                    stem.x() + SPACE * 0.95, y - direction * SPACE * 0.15,
                    stem.x() + SPACE * 1.05, y - direction * SPACE * 0.75,
                    stem.x() + SPACE * 0.75, y - direction * SPACE * 1.5);
            g.draw(hook);
        }
    }

    private static void paintBeamGroup(
            Graphics2D g,
            ScoreLayout layout,
            Track track,
            Clef clef,
            int trackIndex,
            int measureIndex,
            BeamGroup group) {
        if (group.isSingle()) {
            return;
        }
        Measure measure = track.measure(measureIndex);
        List<Stem> stems = new ArrayList<>();
        boolean up = groupPointsUp(track, clef, measure, group);
        for (int beatIndex = group.firstBeat(); beatIndex <= group.lastBeat(); beatIndex++) {
            stems.add(stemOf(layout, track, clef, trackIndex, measureIndex, beatIndex,
                    measure.beat(beatIndex), up));
        }

        double beamY = up
                ? stems.stream().mapToDouble(Stem::endY).min().orElseThrow()
                : stems.stream().mapToDouble(Stem::endY).max().orElseThrow();

        g.setColor(ScoreColors.INK);
        g.setStroke(STEM);
        for (Stem stem : stems) {
            g.draw(new java.awt.geom.Line2D.Double(stem.x(), stem.rootY(), stem.x(), beamY));
        }

        int beams = sharedBeamCount(measure, group);
        double direction = up ? 1 : -1;
        for (int beam = 0; beam < beams; beam++) {
            double y = beamY + direction * beam * BEAM_GAP;
            g.fill(new java.awt.geom.Rectangle2D.Double(
                    stems.get(0).x() - 0.5,
                    Math.min(y, y + BEAM_THICKNESS) - (up ? 0 : BEAM_THICKNESS),
                    stems.get(stems.size() - 1).x() - stems.get(0).x() + 1,
                    BEAM_THICKNESS));
        }
        paintPartialBeams(g, stems, measure, group, beamY, direction, beams);
    }

    private static int sharedBeamCount(Measure measure, BeamGroup group) {
        int shared = Integer.MAX_VALUE;
        for (int beatIndex = group.firstBeat(); beatIndex <= group.lastBeat(); beatIndex++) {
            shared = Math.min(shared, Beaming.beamCount(measure.beat(beatIndex).duration().value()));
        }
        return Math.max(1, shared);
    }

    private static void paintPartialBeams(
            Graphics2D g,
            List<Stem> stems,
            Measure measure,
            BeamGroup group,
            double beamY,
            double direction,
            int sharedBeams) {
        for (int index = 0; index < stems.size(); index++) {
            int beatIndex = group.firstBeat() + index;
            int beams = Beaming.beamCount(measure.beat(beatIndex).duration().value());
            double x = stems.get(index).x();
            boolean toTheLeft = index == stems.size() - 1;
            for (int beam = sharedBeams; beam < beams; beam++) {
                double y = beamY + direction * beam * BEAM_GAP;
                double stub = SPACE * 0.85;
                g.fill(new java.awt.geom.Rectangle2D.Double(
                        toTheLeft ? x - stub : x - 0.5,
                        y - (direction > 0 ? 0 : BEAM_THICKNESS),
                        stub + 1,
                        BEAM_THICKNESS));
            }
        }
    }

    private static boolean groupPointsUp(Track track, Clef clef, Measure measure, BeamGroup group) {
        double total = 0;
        int count = 0;
        for (int beatIndex = group.firstBeat(); beatIndex <= group.lastBeat(); beatIndex++) {
            for (Note note : measure.beat(beatIndex).notes()) {
                total += positionOf(track, clef, note).step();
                count++;
            }
        }
        return count == 0 || total / count < MIDDLE_LINE_STEP;
    }

    private static Stem stemOf(
            ScoreLayout layout,
            Track track,
            Clef clef,
            int trackIndex,
            int measureIndex,
            int beatIndex,
            Beat beat) {
        return stemOf(layout, track, clef, trackIndex, measureIndex, beatIndex, beat, pointsUp(track, clef, beat));
    }

    private static Stem stemOf(
            ScoreLayout layout,
            Track track,
            Clef clef,
            int trackIndex,
            int measureIndex,
            int beatIndex,
            Beat beat,
            boolean up) {
        double centerX = noteCenterX(layout, trackIndex, measureIndex, beatIndex);
        int highest = beat.notes().stream().mapToInt(note -> positionOf(track, clef, note).step()).max().orElse(4);
        int lowest = beat.notes().stream().mapToInt(note -> positionOf(track, clef, note).step()).min().orElse(4);
        double rootY = layout.stepY(trackIndex, measureIndex, up ? highest : lowest);
        double x = up ? centerX + NOTE_WIDTH / 2 - 0.8 : centerX - NOTE_WIDTH / 2 + 0.8;
        double span = STEM_LENGTH + Math.abs(highest - lowest) * HALF_SPACE;
        return new Stem(x, rootY, up ? rootY - span : rootY + span, up);
    }

    private static boolean pointsUp(Track track, Clef clef, Beat beat) {
        return beat.notes().stream()
                .mapToInt(note -> positionOf(track, clef, note).step())
                .average()
                .orElse(0) < MIDDLE_LINE_STEP;
    }

    private static StaffPosition positionOf(Track track, Clef clef, Note note) {
        return StaffPosition.of(track.tuning().pitchOf(note), clef);
    }

    private static double noteCenterX(ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex) {
        Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, beatIndex);
        return bounds.x + bounds.width / 2.0;
    }

    private static void paintRest(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex, Beat beat) {
        double centerX = noteCenterX(layout, trackIndex, measureIndex, beatIndex);
        g.setColor(ScoreColors.INK);
        switch (beat.duration().value()) {
            case WHOLE -> fillRestBar(g, layout, trackIndex, measureIndex, centerX, 6, true);
            case HALF -> fillRestBar(g, layout, trackIndex, measureIndex, centerX, 4, false);
            case QUARTER -> paintQuarterRest(g, layout, trackIndex, measureIndex, centerX);
            default -> paintHookedRest(g, layout, trackIndex, measureIndex, centerX,
                    Beaming.beamCount(beat.duration().value()));
        }
        if (beat.duration().dotted()) {
            fill(g, dot(centerX + SPACE * 1.1, layout.stepY(trackIndex, measureIndex, 5), SPACE * 0.17));
        }
    }

    private static void fillRestBar(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, double centerX, int step,
            boolean hanging) {
        double y = layout.stepY(trackIndex, measureIndex, step);
        double height = SPACE * 0.5;
        g.fill(new java.awt.geom.Rectangle2D.Double(
                centerX - SPACE * 0.6, hanging ? y : y - height, SPACE * 1.2, height));
    }

    private static void paintQuarterRest(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, double centerX) {
        double top = layout.stepY(trackIndex, measureIndex, 7);
        Path2D rest = new Path2D.Double();
        rest.moveTo(centerX - SPACE * 0.30, top);
        rest.lineTo(centerX + SPACE * 0.32, top + SPACE * 0.85);
        rest.lineTo(centerX - SPACE * 0.26, top + SPACE * 1.60);
        rest.lineTo(centerX + SPACE * 0.36, top + SPACE * 2.35);
        rest.curveTo(
                centerX - SPACE * 0.32, top + SPACE * 2.10,
                centerX - SPACE * 0.34, top + SPACE * 3.05,
                centerX + SPACE * 0.30, top + SPACE * 3.20);
        g.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(rest);
    }

    private static void paintHookedRest(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, double centerX, int hooks) {
        double top = layout.stepY(trackIndex, measureIndex, 6 - (hooks - 1));
        double bottom = layout.stepY(trackIndex, measureIndex, 2);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.Line2D.Double(
                centerX + SPACE * 0.42, top, centerX - SPACE * 0.30, bottom));
        for (int hook = 0; hook < hooks; hook++) {
            double y = top + hook * SPACE;
            fill(g, dot(centerX - SPACE * 0.10, y + SPACE * 0.10, SPACE * 0.20));
            g.draw(new java.awt.geom.Line2D.Double(
                    centerX - SPACE * 0.10, y + SPACE * 0.10, centerX + SPACE * 0.40, y - SPACE * 0.10));
        }
    }

    private static Shape dot(double x, double y, double radius) {
        return new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
    }

    private static void fill(Graphics2D g, Shape shape) {
        g.fill(shape);
    }

    private static Shape trebleClef(double x, double gLineY) {
        double u = SPACE;
        double cx = x + 1.35 * u;
        Path2D clef = new Path2D.Double();

        clef.moveTo(cx + 0.50 * u, gLineY - 4.20 * u);
        clef.curveTo(
                cx + 1.02 * u, gLineY - 4.00 * u,
                cx + 0.98 * u, gLineY - 3.10 * u,
                cx + 0.42 * u, gLineY - 2.50 * u);
        clef.curveTo(
                cx - 0.08 * u, gLineY - 1.95 * u,
                cx - 0.08 * u, gLineY - 0.30 * u,
                cx + 0.04 * u, gLineY + 0.90 * u);
        clef.curveTo(
                cx + 0.16 * u, gLineY + 1.90 * u,
                cx + 0.38 * u, gLineY + 2.35 * u,
                cx - 0.12 * u, gLineY + 2.78 * u);
        clef.curveTo(
                cx - 0.50 * u, gLineY + 3.08 * u,
                cx - 0.92 * u, gLineY + 2.52 * u,
                cx - 0.72 * u, gLineY + 2.10 * u);

        clef.append(spiral(cx, gLineY, 1.18 * u, 0.16 * u, -80, 1.6), false);
        return clef;
    }

    /** La voluta de la clave de sol: una espiral que se cierra sobre la linea de sol. */
    private static Path2D spiral(
            double cx, double cy, double outerRadius, double innerRadius, double startDegrees, double turns) {
        Path2D path = new Path2D.Double();
        int steps = 80;
        for (int step = 0; step <= steps; step++) {
            double progress = (double) step / steps;
            double angle = Math.toRadians(startDegrees) - progress * turns * 2 * Math.PI;
            double radius = outerRadius + progress * (innerRadius - outerRadius);
            double px = cx + radius * Math.cos(angle);
            double py = cy + radius * Math.sin(angle);
            if (step == 0) {
                path.moveTo(px, py);
            } else {
                path.lineTo(px, py);
            }
        }
        return path;
    }

    private static Shape bassClef(double x, double fLineY) {
        double u = SPACE;
        double cx = x + 1.05 * u;
        Path2D clef = new Path2D.Double();
        clef.moveTo(cx - 0.95 * u, fLineY - 0.35 * u);
        clef.curveTo(
                cx - 0.60 * u, fLineY - 1.25 * u,
                cx + 0.60 * u, fLineY - 1.10 * u,
                cx + 0.58 * u, fLineY - 0.10 * u);
        clef.curveTo(
                cx + 0.56 * u, fLineY + 1.15 * u,
                cx - 0.30 * u, fLineY + 1.95 * u,
                cx - 1.20 * u, fLineY + 2.25 * u);
        return clef;
    }

    private record Stem(double x, double rootY, double endY, boolean up) {
    }
}
