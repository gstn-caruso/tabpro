package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.effects.Bend;
import com.gstncaruso.tabpro.core.model.effects.Finger;
import com.gstncaruso.tabpro.core.model.effects.GraceTransition;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.SlideType;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.Optional;

/**
 * Lo que el manual dibuja directamente sobre la tablatura, ademas del numero de traste: ligados,
 * slides, bends con su altura, la palanca, notas de adorno con su transicion y como se digita
 * con las dos manos.
 */
final class TabNotationPainter {

    private static final Font FINGER_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
    private static final Font BEND_FONT = new Font(Font.SANS_SERIF, Font.ITALIC, 9);
    private static final Font GRACE_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
    private static final int FINGER_RADIUS = 6;

    /** Cuanto cuelga bajo la tablatura la curva de la palanca. */
    private static final int BAR_CURVE_HEIGHT = 10;

    private static final int UPWARDS = -1;
    private static final int DOWNWARDS = 1;

    private TabNotationPainter() {
    }

    static void paintMeasure(Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex) {
        Measure measure = track.measure(measureIndex);
        Optional<Beat> nextMeasureFirstBeat = track.measureCount() > measureIndex + 1
                ? Optional.of(track.measure(measureIndex + 1).beat(0))
                : Optional.empty();

        for (int index = 0; index < measure.beats().size(); index++) {
            final int beatIndex = index;
            Beat beat = measure.beat(beatIndex);
            Optional<Beat> nextBeat = beatIndex + 1 < measure.beats().size()
                    ? Optional.of(measure.beat(beatIndex + 1))
                    : nextMeasureFirstBeat;
            int nextBeatIndex = beatIndex + 1 < measure.beats().size() ? beatIndex + 1 : -1;

            beat.effects().tremoloBar().ifPresent(bar ->
                    paintTremoloBar(g, layout, trackIndex, measureIndex, beatIndex, bar));

            for (Note note : beat.notes()) {
                paintFingering(g, layout, trackIndex, measureIndex, beatIndex, note);
                paintGraceNote(g, layout, trackIndex, measureIndex, beatIndex, note);
                note.effects().bend().ifPresent(bend ->
                        paintBend(g, layout, trackIndex, measureIndex, beatIndex, note, bend));

                Optional<Note> next = nextBeat.flatMap(b -> b.noteOn(note.string()));
                if (next.isEmpty()) {
                    continue;
                }
                boolean sameMeasure = nextBeatIndex >= 0;
                int nextIndex = sameMeasure ? nextBeatIndex : beatIndex;
                if (note.has(Ornament.HAMMER_ON_PULL_OFF) || next.get().tied()) {
                    paintSlur(g, layout, trackIndex, measureIndex, beatIndex, nextIndex, note.string(), sameMeasure);
                }
                note.effects().slide().ifPresent(slide -> {
                    if (slide.towardsTheNextNote() && sameMeasure) {
                        paintSlideToNext(
                                g, layout, trackIndex, measureIndex, beatIndex, nextIndex, note.string(), slide);
                    }
                });
            }
            for (Note note : beat.notes()) {
                note.effects().slide()
                        .filter(slide -> !slide.towardsTheNextNote())
                        .ifPresent(slide -> paintOpenSlide(g, layout, trackIndex, measureIndex, beatIndex, note, slide));
            }
        }
    }

    private static void paintFingering(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex, Note note) {
        Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, beatIndex);
        int centerX = bounds.x + bounds.width / 2;
        int tabBottom = layout.tabBottom(trackIndex, measureIndex);

        Optional<Finger> left = note.effects().leftHand();
        Optional<Finger> right = note.effects().rightHand();
        if (left.isPresent()) {
            paintFingerCircle(g, centerX, tabBottom + 10, left.get().leftHandSymbol());
        }
        if (right.isPresent()) {
            paintFingerCircle(g, centerX, tabBottom + (left.isPresent() ? 24 : 10), right.get().rightHandSymbol());
        }
    }

    private static void paintFingerCircle(Graphics2D g, int centerX, int y, String label) {
        g.setColor(ScoreColors.LABEL);
        g.setStroke(new BasicStroke(1f));
        g.draw(new Ellipse2D.Double(centerX - FINGER_RADIUS, y - FINGER_RADIUS, FINGER_RADIUS * 2, FINGER_RADIUS * 2));
        g.setFont(FINGER_FONT);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(label, centerX - metrics.stringWidth(label) / 2, y + (metrics.getAscent() - metrics.getDescent()) / 2);
    }

    private static void paintGraceNote(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex, Note note) {
        note.effects().grace().ifPresent(grace -> {
            Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, beatIndex);
            int y = layout.stringY(trackIndex, measureIndex, note.string());
            int x = bounds.x - 3;
            g.setColor(ScoreColors.LABEL);
            g.setFont(GRACE_FONT);
            String text = String.valueOf(grace.fret());
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(text, x - metrics.stringWidth(text), y + metrics.getAscent() / 2 - 1);
            paintGraceTransition(g, grace.transition(), bounds.x + 2, bounds.x + bounds.width / 2 - 5, y);
        });
    }

    /**
     * Como se llega desde la nota de adorno hasta la nota: el ligado con una
     * ligadura, el slide con su raya y el bend con la curva que le es propia.
     * Sin transicion no hay nada que dibujar entre las dos.
     */
    private static void paintGraceTransition(
            Graphics2D g, GraceTransition transition, int fromX, int toX, int y) {
        if (toX <= fromX) {
            return;
        }
        g.setColor(ScoreColors.LABEL);
        g.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (transition) {
            case SLIDE -> g.draw(new Line2D.Double(fromX, y + 4, toX, y - 4));
            case HAMMER -> g.draw(new Arc2D.Double(fromX, y - 9, toX - fromX, 10, 20, 140, Arc2D.OPEN));
            case BEND -> {
                Path2D curve = new Path2D.Double();
                curve.moveTo(fromX, y);
                curve.curveTo(fromX + (toX - fromX) / 2.0, y, toX, y, toX, y - 8);
                g.draw(curve);
                paintArrowhead(g, toX, y - 8, UPWARDS);
            }
            case NONE -> {
            }
        }
    }

    private static void paintBend(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex, Note note, Bend bend) {
        Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, beatIndex);
        int x = bounds.x + bounds.width / 2 + 6;
        int y = layout.stringY(trackIndex, measureIndex, note.string()) - ScoreLayout.STRING_SPACING / 2;
        int top = y - 12;

        g.setColor(ScoreColors.LABEL);
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D curve = new Path2D.Double();
        curve.moveTo(x, y);
        curve.curveTo(x + 4, top, x + 8, top, x + 10, top);
        g.draw(curve);
        paintArrowhead(g, x + 10, top, UPWARDS);

        String label = bendLabel(bend.peakQuarterTones());
        g.setFont(BEND_FONT);
        g.drawString(label, x + 12, top + 3);
    }

    /**
     * La palanca se anota como el bend, pero vale para el beat entero y cuelga
     * bajo la tablatura: la curva sale del centro del beat hacia donde lleva la
     * altura y al lado va cuanto se aparta.
     */
    private static void paintTremoloBar(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex, Bend bar) {
        Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, beatIndex);
        int x = bounds.x + bounds.width / 2 + 6;
        int top = layout.tabBottom(trackIndex, measureIndex) + 6;
        int bottom = top + BAR_CURVE_HEIGHT;
        int quarterTones = bar.farthestQuarterTones();
        boolean dives = quarterTones < 0;
        int from = dives ? top : bottom;
        int to = dives ? bottom : top;

        g.setColor(ScoreColors.LABEL);
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D curve = new Path2D.Double();
        curve.moveTo(x, from);
        curve.curveTo(x + 4, to, x + 8, to, x + 10, to);
        g.draw(curve);
        paintArrowhead(g, x + 10, to, dives ? DOWNWARDS : UPWARDS);

        g.setFont(BEND_FONT);
        g.drawString(signedBendLabel(quarterTones), x + 12, bottom);
    }

    private static void paintArrowhead(Graphics2D g, int x, int y, int direction) {
        Path2D head = new Path2D.Double();
        head.moveTo(x - 3, y - 3 * direction);
        head.lineTo(x, y);
        head.lineTo(x + 1, y - 4 * direction);
        g.draw(head);
    }

    /** Lo mismo que {@link #bendLabel}, pero para una curva que puede bajar, como la palanca. */
    static String signedBendLabel(int quarterTones) {
        return quarterTones < 0 ? "-" + bendLabel(-quarterTones) : bendLabel(quarterTones);
    }

    /** Cuanto sube el bend, en la notacion habitual: cuartos, medios y enteros de tono. */
    static String bendLabel(int quarterTones) {
        double tones = quarterTones / 4.0;
        if (tones == Math.floor(tones)) {
            return tones == 1.0 ? "full" : String.valueOf((int) tones);
        }
        if (Math.abs(tones - 0.25) < 1e-9) {
            return "¼";
        }
        if (Math.abs(tones - 0.5) < 1e-9) {
            return "½";
        }
        if (Math.abs(tones - 1.5) < 1e-9) {
            return "1½";
        }
        return String.valueOf(tones);
    }

    private static void paintSlur(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int fromBeat, int toBeat,
            int string, boolean sameMeasure) {
        Rectangle from = layout.beatBounds(trackIndex, measureIndex, fromBeat);
        int fromX = from.x + from.width / 2 + 5;
        int toX;
        if (sameMeasure) {
            Rectangle to = layout.beatBounds(trackIndex, measureIndex, toBeat);
            toX = to.x + to.width / 2 - 5;
        } else {
            toX = from.x + from.width;
        }
        if (toX <= fromX) {
            return;
        }
        int y = layout.stringY(trackIndex, measureIndex, string) - ScoreLayout.STRING_SPACING / 2 - 1;
        g.setColor(ScoreColors.LABEL);
        g.setStroke(new BasicStroke(1.1f));
        g.draw(new Arc2D.Double(fromX, y - 6, toX - fromX, 10, 20, 140, Arc2D.OPEN));
    }

    /**
     * El legato y el shift slide van los dos hacia la nota siguiente, pero el manual los
     * distingue: en el legato la nota de destino no se ataca de nuevo, en el shift si
     * ({@link SlideType#picksTheDestination()}). Esa diferencia se ve en la raya: solida para uno,
     * cortada para el otro.
     */
    private static void paintSlideToNext(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int fromBeat, int toBeat, int string,
            SlideType slide) {
        Rectangle from = layout.beatBounds(trackIndex, measureIndex, fromBeat);
        Rectangle to = layout.beatBounds(trackIndex, measureIndex, toBeat);
        int y = layout.stringY(trackIndex, measureIndex, string);
        int fromX = from.x + from.width / 2 + 6;
        int toX = to.x + to.width / 2 - 6;
        if (toX <= fromX) {
            return;
        }
        g.setColor(ScoreColors.LABEL);
        g.setStroke(slide.picksTheDestination()
                ? new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {3f, 2f}, 0f)
                : new BasicStroke(1.4f));
        g.draw(new Line2D.Double(fromX, y + 3, toX, y - 3));
    }

    private static void paintOpenSlide(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex, Note note, SlideType slide) {
        Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, beatIndex);
        int centerX = bounds.x + bounds.width / 2;
        int y = layout.stringY(trackIndex, measureIndex, note.string());
        g.setColor(ScoreColors.LABEL);
        g.setStroke(new BasicStroke(1.3f));
        int half = 7;
        Line2D line = switch (slide) {
            case IN_FROM_BELOW -> new Line2D.Double(centerX - half - 4, y + 4, centerX - 4, y);
            case IN_FROM_ABOVE -> new Line2D.Double(centerX - half - 4, y - 4, centerX - 4, y);
            case OUT_DOWNWARDS -> new Line2D.Double(centerX + 4, y, centerX + half + 4, y + 4);
            case OUT_UPWARDS -> new Line2D.Double(centerX + 4, y, centerX + half + 4, y - 4);
            default -> null;
        };
        if (line != null) {
            g.draw(line);
        }
    }
}
