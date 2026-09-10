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
import com.gstncaruso.tabpro.core.model.bars.OctaveMark;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.StemOverride;
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
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
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
    private static final double STEM_LENGTH = SPACE * 3.4;
    private static final double BEAM_THICKNESS = SPACE * 0.52;
    private static final double BEAM_GAP = SPACE * 0.84;
    /** El tope de inclinacion de una barra de union: un espacio de pentagrama por grupo, la
     * convencion tipografica habitual (nunca mas de dos). */
    private static final double MAX_BEAM_SLOPE = SPACE;
    private static final int MIDDLE_LINE_STEP = 4;

    /** Los grados donde va cada alteracion de la armadura, en orden de letra (Do..Si). */
    private static final int[] TREBLE_KEY_STEPS = {5, 6, 7, 8, 9, 3, 4};
    private static final int[] BASS_KEY_STEPS = {3, 4, 5, 6, 7, 1, 2};

    private static final BasicStroke THIN = new BasicStroke(1f);
    private static final BasicStroke STEM = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke DOTTED = new BasicStroke(
            1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] {1.5f, 2.5f}, 0f);
    private static final Font OCTAVE_MARK_FONT = ScoreFonts.octaveMarkFont(SPACE);
    /** El aire entre el pentagrama y la marca de octava (rotulo + linea de puntos). */
    private static final double OCTAVE_MARK_GAP = SPACE * 0.8;

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
        g.setFont(MusicFont.sizedTo(SPACE));
        String glyph = clef == Clef.TREBLE ? MusicFont.trebleClef() : MusicFont.bassClef();
        int line = clef == Clef.TREBLE ? 1 : 3;
        g.drawString(glyph, (float) x, layout.staffLineY(trackIndex, measureIndex, line));
    }

    static void paintTimeSignature(
            Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex, double x) {
        Measure measure = track.measure(measureIndex);
        g.setColor(ScoreColors.INK);
        g.setFont(MusicFont.sizedTo(SPACE));
        FontMetrics metrics = g.getFontMetrics();

        String top = timeSignatureGlyphsOf(measure.timeSignature().beats());
        String bottom = timeSignatureGlyphsOf(measure.timeSignature().beatUnit());
        int centerX = (int) Math.round(x + Math.max(metrics.stringWidth(top), metrics.stringWidth(bottom)) / 2.0);
        int upperY = layout.staffLineY(trackIndex, measureIndex, 3);
        int lowerY = layout.staffLineY(trackIndex, measureIndex, 1);
        g.drawString(top, centerX - metrics.stringWidth(top) / 2, upperY);
        g.drawString(bottom, centerX - metrics.stringWidth(bottom) / 2, lowerY);
    }

    /** Una cifra de compas armada glifo por glifo, uno por cada digito del numero. */
    private static String timeSignatureGlyphsOf(int number) {
        StringBuilder glyphs = new StringBuilder();
        for (char digit : String.valueOf(number).toCharArray()) {
            glyphs.append(MusicFont.timeSignatureDigit(digit - '0'));
        }
        return glyphs.toString();
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
        OctaveMark octaveMark = measure.attributes().octaveMark();
        int octaveShift = octaveMark.staffStepShift();

        paintVoice(g, layout, track, clef, trackIndex, measureIndex, VoicePart.LEAD, measure.lead(),
                accidentals, twoVoices, dims(highlightedVoice, VoicePart.LEAD) && twoVoices,
                laneCount, measure.timeSignature(), octaveShift);
        if (twoVoices) {
            paintVoice(g, layout, track, clef, trackIndex, measureIndex, VoicePart.BASS, measure.voice(VoicePart.BASS),
                    accidentals, true, dims(highlightedVoice, VoicePart.BASS), laneCount, measure.timeSignature(),
                    octaveShift);
        }
        paintTupletBrackets(g, layout, track, clef, trackIndex, measureIndex, measure);
        paintOctaveMark(g, layout, octaveMark, trackIndex, measureIndex);
    }

    /** Se atenua toda voz que no sea la destacada; si no hay destacada, no se atenua ninguna. */
    private static boolean dims(Optional<VoicePart> highlightedVoice, VoicePart part) {
        return highlightedVoice.isPresent() && highlightedVoice.get() != part;
    }

    private static void paintVoice(
            Graphics2D g, ScoreLayout layout, Track track, Clef clef, int trackIndex, int measureIndex,
            VoicePart part, Voice voice, KeySignatureAccidentals accidentals, boolean twoVoices, boolean dimmed,
            int laneCount, TimeSignature timeSignature, int octaveShift) {
        Color ink = dimmed ? ScoreColors.VOICE_INACTIVE : ScoreColors.INK;
        List<Beat> beats = voice.beats();

        for (int beatIndex = 0; beatIndex < beats.size(); beatIndex++) {
            Beat beat = beats.get(beatIndex);
            int lane = Math.min(beatIndex, laneCount - 1);
            if (beat.isRest()) {
                paintRest(g, layout, trackIndex, measureIndex, lane, beat, ink);
            } else {
                paintNoteheads(g, layout, track, clef, trackIndex, measureIndex, lane, beat, accidentals, ink, dimmed,
                        octaveShift);
            }
        }
        paintTies(g, layout, track, clef, trackIndex, measureIndex, beats, laneCount, ink, octaveShift);

        List<BeamGroup> groups = groupsFor(timeSignature, beats);
        for (BeamGroup group : groups) {
            paintBeamGroup(g, layout, track, clef, trackIndex, measureIndex, beats, group, laneCount, part, twoVoices,
                    ink, octaveShift);
        }
        paintUnbeamedStems(g, layout, track, clef, trackIndex, measureIndex, beats, groups, laneCount, part, twoVoices,
                ink, octaveShift);
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
            Color ink,
            boolean dimmed,
            int octaveShift) {
        double centerX = noteCenterX(layout, trackIndex, measureIndex, beatIndex);
        String notehead = noteheadGlyphFor(beat.duration().value());
        // "Ver > Notas con dinamica [F11]": solo la cabeza de la nota cambia de tinta, y solo
        // cuando no esta atenuada -la voz que no se edita se sigue viendo pareja, sin importar
        // cuan fuerte suena cada una de sus notas.
        boolean colorsByDynamic = layout.showsDynamicNotes() && !dimmed;

        for (Note note : beat.notes()) {
            StaffPosition position = positionOf(track, clef, note, octaveShift);
            double y = layout.stepY(trackIndex, measureIndex, position.step());
            paintLedgerLines(g, layout, trackIndex, measureIndex, position, centerX, ink);
            AccidentalGlyph glyph = accidentals.glyphFor(position);
            if (glyph != AccidentalGlyph.NONE) {
                paintAccidental(g, glyph, centerX - NOTE_WIDTH * 0.75 - SPACE * 0.55, y, ink);
            }
            Color headInk = colorsByDynamic ? ScoreColors.forDynamic(note.effects().dynamic()) : ink;
            paintNotehead(g, centerX, y, notehead, headInk);
            if (beat.duration().dotted()) {
                paintDot(g, layout, trackIndex, measureIndex, position, centerX, ink);
            }
            paintArticulations(g, note, centerX, y, position.step(), ink);
        }
    }

    private static void paintArticulations(Graphics2D g, Note note, double centerX, double y, int step, Color ink) {
        boolean stemPointsUp = step < MIDDLE_LINE_STEP;
        double accentY = stemPointsUp ? y - NOTE_HEIGHT - SPACE * 0.35 : y + NOTE_HEIGHT + SPACE * 0.35;
        if (note.has(Ornament.STACCATO)) {
            boolean dotAboveTheHead = !stemPointsUp;
            double dotY = dotAboveTheHead ? y - NOTE_HEIGHT - SPACE * 0.35 : y + NOTE_HEIGHT + SPACE * 0.35;
            paintArticulationGlyph(g, centerX, dotY,
                    dotAboveTheHead ? MusicFont.articStaccatoAbove() : MusicFont.articStaccatoBelow(), ink);
        }
        if (note.has(Ornament.ACCENTED) || note.has(Ornament.HEAVY_ACCENTED)) {
            paintAccentMark(g, centerX, accentY, stemPointsUp, ink, note.has(Ornament.HEAVY_ACCENTED));
        }
    }

    private static void paintArticulationGlyph(Graphics2D g, double centerX, double y, String glyph, Color ink) {
        g.setColor(ink);
        g.setFont(MusicFont.sizedTo(SPACE));
        double width = g.getFontMetrics().stringWidth(glyph);
        g.drawString(glyph, (float) (centerX - width / 2), (float) y);
    }

    private static void paintAccentMark(Graphics2D g, double centerX, double y, boolean above, Color ink, boolean heavy) {
        String glyph = above ? MusicFont.articAccentAbove() : MusicFont.articAccentBelow();
        paintArticulationGlyph(g, centerX, y, glyph, ink);
        if (heavy) {
            paintArticulationGlyph(g, centerX, y - SPACE * 0.5, glyph, ink);
        }
    }

    private static String noteheadGlyphFor(NoteValue value) {
        return switch (value) {
            case WHOLE -> MusicFont.noteheadWhole();
            case HALF -> MusicFont.noteheadHalf();
            default -> MusicFont.noteheadBlack();
        };
    }

    private static void paintNotehead(Graphics2D g, double centerX, double y, String glyph, Color ink) {
        g.setColor(ink);
        g.setFont(MusicFont.sizedTo(SPACE));
        double width = g.getFontMetrics().stringWidth(glyph);
        g.drawString(glyph, (float) (centerX - width / 2), (float) y);
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
        paintAugmentationDot(g, centerX + NOTE_WIDTH * 0.85, y, ink);
    }

    private static void paintAugmentationDot(Graphics2D g, double x, double y, Color ink) {
        g.setColor(ink);
        g.setFont(MusicFont.sizedTo(SPACE));
        g.drawString(MusicFont.augmentationDot(), (float) x, (float) y);
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
        paintAccidentalGlyph(g, MusicFont.accidentalSharp(), x, y, ink);
    }

    private static void paintAccidentalGlyph(Graphics2D g, String glyph, double x, double y, Color ink) {
        g.setColor(ink);
        g.setFont(MusicFont.sizedTo(SPACE));
        g.drawString(glyph, (float) x, (float) y);
    }

    private static void paintFlat(Graphics2D g, double x, double y, Color ink) {
        paintAccidentalGlyph(g, MusicFont.accidentalFlat(), x, y, ink);
    }

    private static void paintNatural(Graphics2D g, double x, double y, Color ink) {
        paintAccidentalGlyph(g, MusicFont.accidentalNatural(), x, y, ink);
    }

    /** Los arcos de ligadura de prolongacion, entre golpes consecutivos de la misma cuerda. */
    private static void paintTies(
            Graphics2D g, ScoreLayout layout, Track track, Clef clef, int trackIndex, int measureIndex,
            List<Beat> beats, int laneCount, Color ink, int octaveShift) {
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
                StaffPosition position = positionOf(track, clef, origin.get(), octaveShift);
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
        int octaveShift = measure.attributes().octaveMark().staffStepShift();
        for (TupletGroup group : Tuplets.groupsOf(measure)) {
            int highestStep = highestStepIn(track, clef, measure, group, octaveShift);
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

            g.setFont(ScoreFonts.TUPLET_FONT);
            FontMetrics metrics = g.getFontMetrics();
            String label = String.valueOf(group.tuplet().enters());
            g.drawString(label, (float) (midX - metrics.stringWidth(label) / 2.0), (float) (y + 4));
        }
    }

    private static int highestStepIn(Track track, Clef clef, Measure measure, TupletGroup group, int octaveShift) {
        int highest = MIDDLE_LINE_STEP;
        boolean any = false;
        for (int beatIndex = group.firstBeat(); beatIndex <= group.lastBeat(); beatIndex++) {
            for (Note note : measure.beat(beatIndex).notes()) {
                int step = positionOf(track, clef, note, octaveShift).step();
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
            Color ink,
            int octaveShift) {
        for (int beatIndex = 0; beatIndex < beats.size(); beatIndex++) {
            Beat beat = beats.get(beatIndex);
            if (beat.isRest() || beat.duration().value() == NoteValue.WHOLE || inABeam(groups, beatIndex)) {
                continue;
            }
            int lane = Math.min(beatIndex, laneCount - 1);
            Stem stem = stemOf(layout, track, clef, trackIndex, measureIndex, lane, beat, part, twoVoices, octaveShift);
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
        switch (flags) {
            case 0 -> {
            }
            case 1 -> paintFlagGlyph(g, stem, MusicFont.flag8thUp(), MusicFont.flag8thDown(), ink);
            case 2 -> paintFlagGlyph(g, stem, MusicFont.flag16thUp(), MusicFont.flag16thDown(), ink);
            case 3 -> paintFlagGlyph(g, stem, MusicFont.flag32ndUp(), MusicFont.flag32ndDown(), ink);
            default -> paintFlagGlyph(g, stem, MusicFont.flag64thUp(), MusicFont.flag64thDown(), ink);
        }
    }

    private static void paintFlagGlyph(Graphics2D g, Stem stem, String up, String down, Color ink) {
        g.setColor(ink);
        g.setFont(MusicFont.sizedTo(SPACE));
        g.drawString(stem.up() ? up : down, (float) stem.x(), (float) stem.endY());
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
            Color ink,
            int octaveShift) {
        if (group.isSingle()) {
            return;
        }
        List<Stem> stems = new ArrayList<>();
        boolean up = groupPointsUp(track, clef, beats, group, part, twoVoices, octaveShift);
        for (int beatIndex = group.firstBeat(); beatIndex <= group.lastBeat(); beatIndex++) {
            int lane = Math.min(beatIndex, laneCount - 1);
            stems.add(stemOf(layout, track, clef, trackIndex, measureIndex, lane, beats.get(beatIndex), up, octaveShift));
        }

        BeamLine beamLine = beamLineFor(
                track, clef, beats, group, stems, up, octaveShift, track.settings().display().forceHorizontalBeams());

        g.setColor(ink);
        g.setStroke(STEM);
        for (Stem stem : stems) {
            g.draw(new Line2D.Double(stem.x(), stem.rootY(), stem.x(), beamLine.yAt(stem.x())));
        }

        int beams = sharedBeamCount(beats, group);
        double direction = up ? 1 : -1;
        for (int beam = 0; beam < beams; beam++) {
            double offset = direction * beam * BEAM_GAP;
            fillBeam(g, beamLine.firstX(), beamLine.firstY() + offset, beamLine.lastX(), beamLine.lastY() + offset,
                    direction);
        }
        paintPartialBeams(g, stems, beats, group, beamLine, direction, beams);
    }

    /**
     * La barra de un grupo: sigue la pendiente que dan las cabezas de nota del primer y el ultimo
     * beat, acotada a {@link #MAX_BEAM_SLOPE}, y horizontal cuando el rasgo de la pista lo fuerza,
     * cuando las notas estan todas a la misma altura o cuando el grupo hace zigzag (las notas de
     * adentro cruzan la linea que uniria los extremos). El extremo que ya queda mas lejos de las
     * cabezas de nota (el que fijaria la barra horizontal de siempre) se mantiene sin estirar de
     * mas; el otro extremo es el que se acerca, nunca mas alla de lo que ya alcanzaba solo.
     */
    private static BeamLine beamLineFor(
            Track track,
            Clef clef,
            List<Beat> beats,
            BeamGroup group,
            List<Stem> stems,
            boolean up,
            int octaveShift,
            boolean forceHorizontalBeams) {
        double flatY = up
                ? stems.stream().mapToDouble(Stem::endY).min().orElseThrow()
                : stems.stream().mapToDouble(Stem::endY).max().orElseThrow();
        double firstX = stems.get(0).x();
        double lastX = stems.get(stems.size() - 1).x();
        if (forceHorizontalBeams || stems.size() < 2) {
            return new BeamLine(firstX, flatY, lastX, flatY);
        }
        List<Integer> outerSteps = outerStepsOf(track, clef, beats, group, up, octaveShift);
        if (isFlatTrend(outerSteps)) {
            return new BeamLine(firstX, flatY, lastX, flatY);
        }

        double naturalFirst = stems.get(0).endY();
        double naturalLast = stems.get(stems.size() - 1).endY();
        boolean firstIsAnchor = up ? naturalFirst <= naturalLast : naturalFirst >= naturalLast;
        double anchor = firstIsAnchor ? naturalFirst : naturalLast;
        double other = firstIsAnchor ? naturalLast : naturalFirst;
        double cappedOther = anchor + clampMagnitude(other - anchor, MAX_BEAM_SLOPE);
        double firstY = firstIsAnchor ? anchor : cappedOther;
        double lastY = firstIsAnchor ? cappedOther : anchor;
        return new BeamLine(firstX, firstY, lastX, lastY);
    }

    private static double clampMagnitude(double value, double cap) {
        return Math.max(-cap, Math.min(cap, value));
    }

    /** El grado mas lejos del centro del pentagrama de cada beat del grupo -el mismo que ata la
     * plica, ver {@link #stemOf}-, en orden: la referencia para decidir si el grupo sube, baja o
     * hace zigzag. */
    private static List<Integer> outerStepsOf(
            Track track, Clef clef, List<Beat> beats, BeamGroup group, boolean up, int octaveShift) {
        List<Integer> steps = new ArrayList<>();
        for (int beatIndex = group.firstBeat(); beatIndex <= group.lastBeat(); beatIndex++) {
            List<Integer> notesSteps = beats.get(beatIndex).notes().stream()
                    .map(note -> positionOf(track, clef, note, octaveShift).step())
                    .toList();
            steps.add(up
                    ? notesSteps.stream().mapToInt(Integer::intValue).max().orElse(MIDDLE_LINE_STEP)
                    : notesSteps.stream().mapToInt(Integer::intValue).min().orElse(MIDDLE_LINE_STEP));
        }
        return steps;
    }

    /** Sin tendencia clara: todas las notas a la misma altura, o un zigzag donde el grado no crece
     * ni decrece de punta a punta sin cambiar de sentido en el medio. */
    private static boolean isFlatTrend(List<Integer> steps) {
        boolean nonDecreasing = true;
        boolean nonIncreasing = true;
        for (int i = 1; i < steps.size(); i++) {
            if (steps.get(i) < steps.get(i - 1)) {
                nonDecreasing = false;
            }
            if (steps.get(i) > steps.get(i - 1)) {
                nonIncreasing = false;
            }
        }
        boolean monotonic = nonDecreasing || nonIncreasing;
        return !monotonic || steps.get(0).equals(steps.get(steps.size() - 1));
    }

    /** El cuerpo de una barra entre sus dos extremos: un rectangulo cuando es horizontal, un
     * paralelogramo cuando tiene pendiente. El espesor crece en la direccion de {@code direction}
     * (hacia las cabezas de nota), nunca hacia afuera de la plica. */
    private static void fillBeam(Graphics2D g, double x1, double y1, double x2, double y2, double direction) {
        double dy = direction * BEAM_THICKNESS;
        java.awt.geom.Path2D.Double body = new java.awt.geom.Path2D.Double();
        body.moveTo(x1 - 0.5, y1);
        body.lineTo(x2 + 0.5, y2);
        body.lineTo(x2 + 0.5, y2 + dy);
        body.lineTo(x1 - 0.5, y1 + dy);
        body.closePath();
        g.fill(body);
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
            BeamLine beamLine,
            double direction,
            int sharedBeams) {
        for (int index = 0; index < stems.size(); index++) {
            int beatIndex = group.firstBeat() + index;
            int beams = Beaming.beamCount(beats.get(beatIndex).duration().value());
            double x = stems.get(index).x();
            boolean toTheLeft = index == stems.size() - 1;
            for (int beam = sharedBeams; beam < beams; beam++) {
                double y = beamLine.yAt(x) + direction * beam * BEAM_GAP;
                double stub = SPACE * 0.85;
                g.fill(new java.awt.geom.Rectangle2D.Double(
                        toTheLeft ? x - stub : x - 0.5,
                        y - (direction > 0 ? 0 : BEAM_THICKNESS),
                        stub + 1,
                        BEAM_THICKNESS));
            }
        }
    }

    /** Los dos extremos de una barra de union, con la pendiente que dan sus dos puntas. */
    private record BeamLine(double firstX, double firstY, double lastX, double lastY) {
        double yAt(double x) {
            if (lastX == firstX) {
                return firstY;
            }
            return firstY + (lastY - firstY) * (x - firstX) / (lastX - firstX);
        }
    }

    /**
     * El manual, linea 923: la direccion de la plica es automatica, pero se puede forzar a mano
     * desde el menu Nota. Una barra de union comparte una sola plica para todo el grupo, asi que
     * el primer override que aparezca entre sus beats -{@link StemOverride#AUTOMATIC} no cuenta-
     * decide por el grupo entero; si ninguno lo pide, sigue la regla automatica de siempre.
     */
    private static boolean groupPointsUp(
            Track track, Clef clef, List<Beat> beats, BeamGroup group, VoicePart part, boolean twoVoices,
            int octaveShift) {
        double total = 0;
        int count = 0;
        for (int beatIndex = group.firstBeat(); beatIndex <= group.lastBeat(); beatIndex++) {
            for (Note note : beats.get(beatIndex).notes()) {
                total += positionOf(track, clef, note, octaveShift).step();
                count++;
            }
        }
        double average = count == 0 ? MIDDLE_LINE_STEP : total / count;
        boolean automatic = StemDirection.pointsUp(part, twoVoices, average, MIDDLE_LINE_STEP);
        return overrideIn(beats, group).pointsUp(automatic);
    }

    private static StemOverride overrideIn(List<Beat> beats, BeamGroup group) {
        for (int beatIndex = group.firstBeat(); beatIndex <= group.lastBeat(); beatIndex++) {
            StemOverride override = beats.get(beatIndex).effects().stemOverride();
            if (override != StemOverride.AUTOMATIC) {
                return override;
            }
        }
        return StemOverride.AUTOMATIC;
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
            boolean twoVoices,
            int octaveShift) {
        double average = beat.notes().stream()
                .mapToInt(note -> positionOf(track, clef, note, octaveShift).step())
                .average()
                .orElse(MIDDLE_LINE_STEP);
        boolean automatic = StemDirection.pointsUp(part, twoVoices, average, MIDDLE_LINE_STEP);
        boolean up = beat.effects().stemOverride().pointsUp(automatic);
        return stemOf(layout, track, clef, trackIndex, measureIndex, beatIndex, beat, up, octaveShift);
    }

    private static Stem stemOf(
            ScoreLayout layout,
            Track track,
            Clef clef,
            int trackIndex,
            int measureIndex,
            int beatIndex,
            Beat beat,
            boolean up,
            int octaveShift) {
        double centerX = noteCenterX(layout, trackIndex, measureIndex, beatIndex);
        int highest = beat.notes().stream().mapToInt(note -> positionOf(track, clef, note, octaveShift).step()).max().orElse(4);
        int lowest = beat.notes().stream().mapToInt(note -> positionOf(track, clef, note, octaveShift).step()).min().orElse(4);
        double rootY = layout.stepY(trackIndex, measureIndex, up ? highest : lowest);
        double x = up ? centerX + NOTE_WIDTH / 2 - 0.8 : centerX - NOTE_WIDTH / 2 + 0.8;
        double span = STEM_LENGTH + Math.abs(highest - lowest) * HALF_SPACE;
        return new Stem(x, rootY, up ? rootY - span : rootY + span, up);
    }

    /**
     * Donde se escribe una nota, con el corrimiento de {@code octaveShift} aplicado -8va/8vb/
     * 15ma/15mb del manual, ver {@link OctaveMark}. Nunca toca {@code track.tuning().pitchOf},
     * que es la altura real: la marca de octava es pura notacion.
     *
     * <p>Sin {@code private}: {@link ScorePainter} la reusa para ubicar, en el pentagrama, la
     * marca de la nota correspondiente al cursor -tiene que coincidir con donde esta clase
     * escribe la cabeza, corrimiento de octava incluido, y no vale repetir el
     * {@code shiftedBySteps} a mano en dos lados.
     */
    static StaffPosition positionOf(Track track, Clef clef, Note note, int octaveShift) {
        return StaffPosition.of(track.tuning().pitchOf(note), clef).shiftedBySteps(octaveShift);
    }

    /**
     * "8va"/"8vb"/"15ma"/"15mb" del manual: el rotulo y su linea de puntos hasta donde alcanza,
     * arriba del pentagrama para lo agudo (8va/15ma, que se escribe mas abajo) y abajo para lo
     * grave (8vb/15mb, que se escribe mas arriba). Puro dibujo: no cambia una sola nota.
     */
    private static void paintOctaveMark(
            Graphics2D g, ScoreLayout layout, OctaveMark octaveMark, int trackIndex, int measureIndex) {
        if (octaveMark == OctaveMark.NONE) {
            return;
        }
        int left = layout.measureX(measureIndex);
        int right = left + layout.measureWidth(measureIndex);

        g.setColor(ScoreColors.INK);
        g.setFont(OCTAVE_MARK_FONT);
        FontMetrics metrics = g.getFontMetrics();
        double baseline = octaveMark.aboveTheStaff()
                ? layout.staffTop(trackIndex, measureIndex) - OCTAVE_MARK_GAP
                : layout.staffBottom(trackIndex, measureIndex) + OCTAVE_MARK_GAP + metrics.getAscent();
        g.drawString(octaveMark.label(), left, (float) baseline);

        double lineY = baseline - metrics.getAscent() * 0.35;
        double lineStart = left + metrics.stringWidth(octaveMark.label()) + SPACE * 0.4;
        if (lineStart < right) {
            g.setStroke(DOTTED);
            g.draw(new Line2D.Double(lineStart, lineY, right, lineY));
        }
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
            case WHOLE -> paintRestGlyph(g, layout, trackIndex, measureIndex, centerX, MusicFont.restWhole(), 6, ink);
            case HALF -> paintRestGlyph(g, layout, trackIndex, measureIndex, centerX, MusicFont.restHalf(), 4, ink);
            case QUARTER -> paintRestGlyph(
                    g, layout, trackIndex, measureIndex, centerX, MusicFont.restQuarter(), MIDDLE_LINE_STEP, ink);
            default -> paintHookedRest(g, layout, trackIndex, measureIndex, centerX,
                    Beaming.beamCount(beat.duration().value()), ink);
        }
        if (beat.duration().dotted()) {
            paintAugmentationDot(g, centerX + SPACE * 1.1, layout.stepY(trackIndex, measureIndex, 5), ink);
        }
    }

    private static void paintRestGlyph(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, double centerX, String glyph,
            int step, Color ink) {
        double y = layout.stepY(trackIndex, measureIndex, step);
        g.setColor(ink);
        g.setFont(MusicFont.sizedTo(SPACE));
        double width = g.getFontMetrics().stringWidth(glyph);
        g.drawString(glyph, (float) (centerX - width / 2), (float) y);
    }

    private static void paintHookedRest(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, double centerX, int hooks, Color ink) {
        switch (hooks) {
            case 1 -> paintRestGlyph(
                    g, layout, trackIndex, measureIndex, centerX, MusicFont.rest8th(), MIDDLE_LINE_STEP, ink);
            case 2 -> paintRestGlyph(
                    g, layout, trackIndex, measureIndex, centerX, MusicFont.rest16th(), MIDDLE_LINE_STEP, ink);
            case 3 -> paintRestGlyph(
                    g, layout, trackIndex, measureIndex, centerX, MusicFont.rest32nd(), MIDDLE_LINE_STEP, ink);
            default -> paintRestGlyph(
                    g, layout, trackIndex, measureIndex, centerX, MusicFont.rest64th(), MIDDLE_LINE_STEP, ink);
        }
    }

    private record Stem(double x, double rootY, double endY, boolean up) {
    }
}
