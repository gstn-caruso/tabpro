package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Voice;
import com.gstncaruso.tabpro.core.model.VoicePart;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.notation.AccidentalGlyph;
import com.gstncaruso.tabpro.core.notation.BeamGroup;
import com.gstncaruso.tabpro.core.notation.Beaming;
import com.gstncaruso.tabpro.core.notation.Clef;
import com.gstncaruso.tabpro.core.notation.KeySignatureAccidentals;
import com.gstncaruso.tabpro.core.notation.StaffPosition;
import com.gstncaruso.tabpro.core.notation.StemDirection;
import com.gstncaruso.tabpro.core.notation.TupletGroup;
import com.gstncaruso.tabpro.core.notation.Tuplets;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** El pentagrama de una pista: clave, armadura, figuras, plicas, barras de union, silencios y
 * las dos voces, cuando la pista las usa. */
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

    /** Los grados donde va cada alteracion de la armadura, en orden de letra (Do..Si). */
    private static final int[] TREBLE_KEY_STEPS = {5, 6, 7, 8, 9, 3, 4};
    private static final int[] BASS_KEY_STEPS = {3, 4, 5, 6, 7, 1, 2};

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

    /** Los sostenidos o los bemoles de la armadura, en el orden convencional de la clave. */
    static void paintKeySignature(
            Graphics2D g, ScoreLayout layout, Clef clef, KeySignature key, int trackIndex, int measureIndex, double x) {
        if (key.alteredCount() == 0) {
            return;
        }
        int[] steps = clef == Clef.TREBLE ? TREBLE_KEY_STEPS : BASS_KEY_STEPS;
        double glyphX = x;
        for (int letter : key.alteredSteps()) {
            double y = layout.stepY(trackIndex, measureIndex, steps[letter]);
            if (key.hasSharps()) {
                paintSharp(g, glyphX, y, ScoreColors.INK);
            } else {
                paintFlat(g, glyphX, y, ScoreColors.INK);
            }
            glyphX += SPACE * 0.95;
        }
    }

    static void paintMeasure(
            Graphics2D g, ScoreLayout layout, Track track, Clef clef, int trackIndex, int measureIndex,
            Optional<VoicePart> highlightedVoice) {
        Measure measure = track.measure(measureIndex);
        boolean twoVoices = measure.usesTwoVoices();
        KeySignatureAccidentals accidentals = new KeySignatureAccidentals(clef, measure.attributes().keySignature());
        // Las dos voces comparten los mismos carriles horizontales de la voz principal, que es
        // la que arma ScoreLayout: funciona sin fisuras cuando comparten subdivision ritmica, que
        // es el caso comun de una melodia con su linea de bajo debajo (limitacion documentada).
        int laneCount = Math.max(1, measure.lead().beatCount());

        paintVoice(g, layout, track, clef, trackIndex, measureIndex, VoicePart.LEAD, measure.lead(),
                accidentals, twoVoices, dims(highlightedVoice, VoicePart.LEAD) && twoVoices,
                laneCount, measure.timeSignature());
        if (twoVoices) {
            paintVoice(g, layout, track, clef, trackIndex, measureIndex, VoicePart.BASS, measure.voice(VoicePart.BASS),
                    accidentals, true, dims(highlightedVoice, VoicePart.BASS), laneCount, measure.timeSignature());
        }
        paintTupletBrackets(g, layout, track, clef, trackIndex, measureIndex, measure);
    }

    /** Se atenua toda voz que no sea la destacada; si no hay destacada, no se atenua ninguna. */
    private static boolean dims(Optional<VoicePart> highlightedVoice, VoicePart part) {
        return highlightedVoice.isPresent() && highlightedVoice.get() != part;
    }

    private static void paintVoice(
            Graphics2D g, ScoreLayout layout, Track track, Clef clef, int trackIndex, int measureIndex,
            VoicePart part, Voice voice, KeySignatureAccidentals accidentals, boolean twoVoices, boolean dimmed,
            int laneCount, TimeSignature timeSignature) {
        Color ink = dimmed ? ScoreColors.VOICE_INACTIVE : ScoreColors.INK;
        List<Beat> beats = voice.beats();

        for (int beatIndex = 0; beatIndex < beats.size(); beatIndex++) {
            Beat beat = beats.get(beatIndex);
            int lane = Math.min(beatIndex, laneCount - 1);
            if (beat.isRest()) {
                paintRest(g, layout, trackIndex, measureIndex, lane, beat, ink);
            } else {
                paintNoteheads(g, layout, track, clef, trackIndex, measureIndex, lane, beat, accidentals, ink);
            }
        }
        paintTies(g, layout, track, clef, trackIndex, measureIndex, beats, laneCount, ink);

        List<BeamGroup> groups = groupsFor(timeSignature, beats);
        for (BeamGroup group : groups) {
            paintBeamGroup(g, layout, track, clef, trackIndex, measureIndex, beats, group, laneCount, part, twoVoices, ink);
        }
        paintUnbeamedStems(g, layout, track, clef, trackIndex, measureIndex, beats, groups, laneCount, part, twoVoices, ink);
    }

    /** Reusa el agrupamiento por barra de {@link Beaming} armando un compas de una sola voz con
     * los beats que corresponda: sirve tanto para la principal como para la de bajos. */
    private static List<BeamGroup> groupsFor(TimeSignature timeSignature, List<Beat> beats) {
        return Beaming.groupsOf(new Measure(timeSignature, beats));
    }

    private static void paintNoteheads(
            Graphics2D g,
            ScoreLayout layout,
            Track track,
            Clef clef,
            int trackIndex,
            int measureIndex,
            int beatIndex,
            Beat beat,
            KeySignatureAccidentals accidentals,
            Color ink) {
        double centerX = noteCenterX(layout, trackIndex, measureIndex, beatIndex);
        boolean hollow = beat.duration().value() == NoteValue.WHOLE
                || beat.duration().value() == NoteValue.HALF;

        for (Note note : beat.notes()) {
            StaffPosition position = positionOf(track, clef, note);
            double y = layout.stepY(trackIndex, measureIndex, position.step());
            paintLedgerLines(g, layout, trackIndex, measureIndex, position, centerX, ink);
            AccidentalGlyph glyph = accidentals.glyphFor(position);
            if (glyph != AccidentalGlyph.NONE) {
                paintAccidental(g, glyph, centerX - NOTE_WIDTH * 0.75 - SPACE * 0.55, y, ink);
            }
            paintNotehead(g, centerX, y, hollow, ink);
            if (beat.duration().dotted()) {
                paintDot(g, layout, trackIndex, measureIndex, position, centerX, ink);
            }
            paintArticulations(g, note, centerX, y, position.step(), ink);
        }
    }

    private static void paintArticulations(Graphics2D g, Note note, double centerX, double y, int step, Color ink) {
        boolean above = step < MIDDLE_LINE_STEP;
        double markY = above ? y - NOTE_HEIGHT - SPACE * 0.35 : y + NOTE_HEIGHT + SPACE * 0.35;
        if (note.has(Ornament.STACCATO)) {
            g.setColor(ink);
            fill(g, dot(centerX, markY, SPACE * 0.16));
        }
        if (note.has(Ornament.ACCENTED) || note.has(Ornament.HEAVY_ACCENTED)) {
            paintAccentMark(g, centerX, markY, ink, note.has(Ornament.HEAVY_ACCENTED));
        }
    }

    private static void paintAccentMark(Graphics2D g, double centerX, double y, Color ink, boolean heavy) {
        g.setColor(ink);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(chevron(centerX, y));
        if (heavy) {
            g.draw(chevron(centerX, y - SPACE * 0.5));
        }
    }

    private static Path2D chevron(double centerX, double y) {
        Path2D chevron = new Path2D.Double();
        chevron.moveTo(centerX - SPACE * 0.5, y - SPACE * 0.25);
        chevron.lineTo(centerX + SPACE * 0.5, y);
        chevron.lineTo(centerX - SPACE * 0.5, y + SPACE * 0.25);
        return chevron;
    }

    private static void paintNotehead(Graphics2D g, double centerX, double y, boolean hollow, Color ink) {
        Shape head = tiltedNotehead(centerX, y);
        g.setColor(ink);
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
            double centerX,
            Color ink) {
        g.setColor(ink);
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
            double centerX,
            Color ink) {
        int step = position.isOnLine() ? position.step() + 1 : position.step();
        double y = layout.stepY(trackIndex, measureIndex, step);
        g.setColor(ink);
        fill(g, dot(centerX + NOTE_WIDTH * 0.85, y, SPACE * 0.17));
    }

    private static void paintAccidental(Graphics2D g, AccidentalGlyph glyph, double x, double y, Color ink) {
        switch (glyph) {
            case SHARP -> paintSharp(g, x, y, ink);
            case FLAT -> paintFlat(g, x, y, ink);
            case NATURAL -> paintNatural(g, x, y, ink);
            case NONE -> {
            }
        }
    }

    private static void paintSharp(Graphics2D g, double x, double y, Color ink) {
        g.setColor(ink);
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

    private static void paintFlat(Graphics2D g, double x, double y, Color ink) {
        g.setColor(ink);
        g.setStroke(new BasicStroke(1.3f));
        g.draw(new Line2D.Double(x, y - SPACE * 1.1, x, y + SPACE * 0.6));
        Path2D bowl = new Path2D.Double();
        bowl.moveTo(x, y + SPACE * 0.55);
        bowl.curveTo(x + SPACE * 0.6, y + SPACE * 0.4, x + SPACE * 0.6, y - SPACE * 0.3, x, y - SPACE * 0.1);
        g.draw(bowl);
    }

    private static void paintNatural(Graphics2D g, double x, double y, Color ink) {
        g.setColor(ink);
        g.setStroke(new BasicStroke(1.1f));
        double half = SPACE * 0.26;
        g.draw(new Line2D.Double(x - half, y - SPACE * 0.85, x - half, y + SPACE * 0.5));
        g.draw(new Line2D.Double(x + half, y - SPACE * 0.5, x + half, y + SPACE * 0.85));
        g.draw(new Line2D.Double(x - half, y + SPACE * 0.3, x + half, y + SPACE * 0.5));
        g.draw(new Line2D.Double(x - half, y - SPACE * 0.5, x + half, y - SPACE * 0.3));
    }

    /** Los arcos de ligadura de prolongacion, entre golpes consecutivos de la misma cuerda. */
    private static void paintTies(
            Graphics2D g, ScoreLayout layout, Track track, Clef clef, int trackIndex, int measureIndex,
            List<Beat> beats, int laneCount, Color ink) {
        for (int i = 0; i + 1 < beats.size(); i++) {
            Beat from = beats.get(i);
            Beat to = beats.get(i + 1);
            for (Note note : to.notes()) {
                if (!note.tied()) {
                    continue;
                }
                Optional<Note> origin = from.noteOn(note.string());
                if (origin.isEmpty()) {
                    continue;
                }
                int laneFrom = Math.min(i, laneCount - 1);
                int laneTo = Math.min(i + 1, laneCount - 1);
                if (laneFrom == laneTo) {
                    continue;
                }
                StaffPosition position = positionOf(track, clef, origin.get());
                double y = layout.stepY(trackIndex, measureIndex, position.step())
                        - (position.step() < MIDDLE_LINE_STEP ? -SPACE * 0.9 : SPACE * 0.9);
                double fromX = noteCenterX(layout, trackIndex, measureIndex, laneFrom) + NOTE_WIDTH * 0.4;
                double toX = noteCenterX(layout, trackIndex, measureIndex, laneTo) - NOTE_WIDTH * 0.4;
                if (toX <= fromX) {
                    continue;
                }
                g.setColor(ink);
                g.setStroke(new BasicStroke(1.1f));
                boolean above = position.step() >= MIDDLE_LINE_STEP;
                double arcHeight = 8;
                g.draw(new Arc2D.Double(fromX, above ? y - arcHeight : y, toX - fromX, arcHeight,
                        above ? 0 : 180, 180, Arc2D.OPEN));
            }
        }
    }

    /** Los corchetes de los grupos irregulares, con su numero en el medio. */
    private static void paintTupletBrackets(
            Graphics2D g, ScoreLayout layout, Track track, Clef clef, int trackIndex, int measureIndex, Measure measure) {
        for (TupletGroup group : Tuplets.groupsOf(measure)) {
            int highestStep = highestStepIn(track, clef, measure, group);
            double y = layout.stepY(trackIndex, measureIndex, highestStep) - SPACE * 2.0;
            double xStart = noteCenterX(layout, trackIndex, measureIndex, group.firstBeat());
            double xEnd = noteCenterX(layout, trackIndex, measureIndex, group.lastBeat());
            double midX = (xStart + xEnd) / 2;

            g.setColor(ScoreColors.INK);
            g.setStroke(THIN);
            g.draw(new Line2D.Double(xStart, y + 4, xStart, y));
            g.draw(new Line2D.Double(xStart, y, midX - 6, y));
            g.draw(new Line2D.Double(midX + 6, y, xEnd, y));
            g.draw(new Line2D.Double(xEnd, y, xEnd, y + 4));

            g.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
            FontMetrics metrics = g.getFontMetrics();
            String label = String.valueOf(group.tuplet().enters());
            g.drawString(label, (float) (midX - metrics.stringWidth(label) / 2.0), (float) (y + 4));
        }
    }

    private static int highestStepIn(Track track, Clef clef, Measure measure, TupletGroup group) {
        int highest = MIDDLE_LINE_STEP;
        boolean any = false;
        for (int beatIndex = group.firstBeat(); beatIndex <= group.lastBeat(); beatIndex++) {
            for (Note note : measure.beat(beatIndex).notes()) {
                int step = positionOf(track, clef, note).step();
                if (!any || step > highest) {
                    highest = step;
                    any = true;
                }
            }
        }
        return highest;
    }

    private static void paintUnbeamedStems(
            Graphics2D g,
            ScoreLayout layout,
            Track track,
            Clef clef,
            int trackIndex,
            int measureIndex,
            List<Beat> beats,
            List<BeamGroup> groups,
            int laneCount,
            VoicePart part,
            boolean twoVoices,
            Color ink) {
        for (int beatIndex = 0; beatIndex < beats.size(); beatIndex++) {
            Beat beat = beats.get(beatIndex);
            if (beat.isRest() || beat.duration().value() == NoteValue.WHOLE || inABeam(groups, beatIndex)) {
                continue;
            }
            int lane = Math.min(beatIndex, laneCount - 1);
            Stem stem = stemOf(layout, track, clef, trackIndex, measureIndex, lane, beat, part, twoVoices);
            g.setColor(ink);
            g.setStroke(STEM);
            g.draw(new Line2D.Double(stem.x(), stem.rootY(), stem.x(), stem.endY()));
            paintFlags(g, stem, Beaming.beamCount(beat.duration().value()), ink);
        }
    }

    private static boolean inABeam(List<BeamGroup> groups, int beatIndex) {
        return groups.stream().anyMatch(group -> !group.isSingle() && group.contains(beatIndex));
    }

    private static void paintFlags(Graphics2D g, Stem stem, int flags, Color ink) {
        if (flags == 0) {
            return;
        }
        double direction = stem.up() ? 1 : -1;
        g.setColor(ink);
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
            List<Beat> beats,
            BeamGroup group,
            int laneCount,
            VoicePart part,
            boolean twoVoices,
            Color ink) {
        if (group.isSingle()) {
            return;
        }
        List<Stem> stems = new ArrayList<>();
        boolean up = groupPointsUp(track, clef, beats, group, part, twoVoices);
        for (int beatIndex = group.firstBeat(); beatIndex <= group.lastBeat(); beatIndex++) {
            int lane = Math.min(beatIndex, laneCount - 1);
            stems.add(stemOf(layout, track, clef, trackIndex, measureIndex, lane, beats.get(beatIndex), up));
        }

        double beamY = up
                ? stems.stream().mapToDouble(Stem::endY).min().orElseThrow()
                : stems.stream().mapToDouble(Stem::endY).max().orElseThrow();

        g.setColor(ink);
        g.setStroke(STEM);
        for (Stem stem : stems) {
            g.draw(new Line2D.Double(stem.x(), stem.rootY(), stem.x(), beamY));
        }

        int beams = sharedBeamCount(beats, group);
        double direction = up ? 1 : -1;
        for (int beam = 0; beam < beams; beam++) {
            double y = beamY + direction * beam * BEAM_GAP;
            g.fill(new java.awt.geom.Rectangle2D.Double(
                    stems.get(0).x() - 0.5,
                    Math.min(y, y + BEAM_THICKNESS) - (up ? 0 : BEAM_THICKNESS),
                    stems.get(stems.size() - 1).x() - stems.get(0).x() + 1,
                    BEAM_THICKNESS));
        }
        paintPartialBeams(g, stems, beats, group, beamY, direction, beams);
    }

    private static int sharedBeamCount(List<Beat> beats, BeamGroup group) {
        int shared = Integer.MAX_VALUE;
        for (int beatIndex = group.firstBeat(); beatIndex <= group.lastBeat(); beatIndex++) {
            shared = Math.min(shared, Beaming.beamCount(beats.get(beatIndex).duration().value()));
        }
        return Math.max(1, shared);
    }

    private static void paintPartialBeams(
            Graphics2D g,
            List<Stem> stems,
            List<Beat> beats,
            BeamGroup group,
            double beamY,
            double direction,
            int sharedBeams) {
        for (int index = 0; index < stems.size(); index++) {
            int beatIndex = group.firstBeat() + index;
            int beams = Beaming.beamCount(beats.get(beatIndex).duration().value());
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

    private static boolean groupPointsUp(
            Track track, Clef clef, List<Beat> beats, BeamGroup group, VoicePart part, boolean twoVoices) {
        double total = 0;
        int count = 0;
        for (int beatIndex = group.firstBeat(); beatIndex <= group.lastBeat(); beatIndex++) {
            for (Note note : beats.get(beatIndex).notes()) {
                total += positionOf(track, clef, note).step();
                count++;
            }
        }
        double average = count == 0 ? MIDDLE_LINE_STEP : total / count;
        return StemDirection.pointsUp(part, twoVoices, average, MIDDLE_LINE_STEP);
    }

    private static Stem stemOf(
            ScoreLayout layout,
            Track track,
            Clef clef,
            int trackIndex,
            int measureIndex,
            int beatIndex,
            Beat beat,
            VoicePart part,
            boolean twoVoices) {
        double average = beat.notes().stream()
                .mapToInt(note -> positionOf(track, clef, note).step())
                .average()
                .orElse(MIDDLE_LINE_STEP);
        boolean up = StemDirection.pointsUp(part, twoVoices, average, MIDDLE_LINE_STEP);
        return stemOf(layout, track, clef, trackIndex, measureIndex, beatIndex, beat, up);
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

    private static StaffPosition positionOf(Track track, Clef clef, Note note) {
        return StaffPosition.of(track.tuning().pitchOf(note), clef);
    }

    private static double noteCenterX(ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex) {
        Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, beatIndex);
        return bounds.x + bounds.width / 2.0;
    }

    private static void paintRest(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex, Beat beat, Color ink) {
        double centerX = noteCenterX(layout, trackIndex, measureIndex, beatIndex);
        g.setColor(ink);
        switch (beat.duration().value()) {
            case WHOLE -> fillRestBar(g, layout, trackIndex, measureIndex, centerX, 6, true, ink);
            case HALF -> fillRestBar(g, layout, trackIndex, measureIndex, centerX, 4, false, ink);
            case QUARTER -> paintQuarterRest(g, layout, trackIndex, measureIndex, centerX, ink);
            default -> paintHookedRest(g, layout, trackIndex, measureIndex, centerX,
                    Beaming.beamCount(beat.duration().value()), ink);
        }
        if (beat.duration().dotted()) {
            g.setColor(ink);
            fill(g, dot(centerX + SPACE * 1.1, layout.stepY(trackIndex, measureIndex, 5), SPACE * 0.17));
        }
    }

    private static void fillRestBar(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, double centerX, int step,
            boolean hanging, Color ink) {
        double y = layout.stepY(trackIndex, measureIndex, step);
        double height = SPACE * 0.5;
        g.setColor(ink);
        g.fill(new java.awt.geom.Rectangle2D.Double(
                centerX - SPACE * 0.6, hanging ? y : y - height, SPACE * 1.2, height));
    }

    private static void paintQuarterRest(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, double centerX, Color ink) {
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
        g.setColor(ink);
        g.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(rest);
    }

    private static void paintHookedRest(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, double centerX, int hooks, Color ink) {
        double top = layout.stepY(trackIndex, measureIndex, 6 - (hooks - 1));
        double bottom = layout.stepY(trackIndex, measureIndex, 2);
        g.setColor(ink);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(
                centerX + SPACE * 0.42, top, centerX - SPACE * 0.30, bottom));
        for (int hook = 0; hook < hooks; hook++) {
            double y = top + hook * SPACE;
            fill(g, dot(centerX - SPACE * 0.10, y + SPACE * 0.10, SPACE * 0.20));
            g.draw(new Line2D.Double(
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
