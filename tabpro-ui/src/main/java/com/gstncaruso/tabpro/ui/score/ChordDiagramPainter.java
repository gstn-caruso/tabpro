package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.TrackDisplay;
import com.gstncaruso.tabpro.core.model.chords.ChordDiagram;
import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;

final class ChordDiagramPainter {

    private static final int STRING_GAP = 6;
    private static final int FRET_GAP = 8;
    private static final int VISIBLE_FRETS = 4;
    private static final int ROW_GAP = 30;
    private static final int STAFF_GAP = 12;

    private ChordDiagramPainter() {
    }

    static void paintMeasure(Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex) {
        TrackDisplay display = track.settings().display();
        if (!display.diagrams().showsOnTheScore()) {
            return;
        }
        Measure measure = track.measure(measureIndex);
        for (int beatIndex = 0; beatIndex < measure.beats().size(); beatIndex++) {
            Beat beat = measure.beat(beatIndex);
            int index = beatIndex;
            beat.effects().chord()
                    .filter(ChordDiagram::shown)
                    .ifPresent(chord -> {
                        Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, index);
                        int gridBottom = display.diagramsBelowStandardNotation()
                                ? layout.staffBottom(trackIndex, measureIndex) + STAFF_GAP + VISIBLE_FRETS * FRET_GAP
                                : layout.staffTop(trackIndex, measureIndex) - STAFF_GAP;
                        paintDiagram(g, bounds.x + bounds.width / 2, gridBottom, chord);
                    });
        }
    }

    static void paintRow(Graphics2D g, List<ChordDiagram> chords, int centerX, int gridBottom) {
        if (chords.isEmpty()) {
            return;
        }
        int totalWidth = chords.stream().mapToInt(ChordDiagramPainter::widthOf).sum()
                + ROW_GAP * (chords.size() - 1);
        int x = centerX - totalWidth / 2;
        for (ChordDiagram chord : chords) {
            int width = widthOf(chord);
            paintDiagram(g, x + width / 2, gridBottom, chord);
            x += width + ROW_GAP;
        }
    }

    private static int widthOf(ChordDiagram chord) {
        return (chord.stringCount() - 1) * STRING_GAP;
    }

    private static void paintDiagram(Graphics2D g, int centerX, int gridBottom, ChordDiagram chord) {
        int stringCount = chord.stringCount();
        int gridWidth = (stringCount - 1) * STRING_GAP;
        int x = centerX - gridWidth / 2;
        int top = gridBottom - VISIBLE_FRETS * FRET_GAP;

        g.setFont(ScoreFonts.CHORD_NAME_FONT);
        g.setColor(ScoreColors.INK);
        FontMetrics nameMetrics = g.getFontMetrics();
        g.drawString(chord.name(), x + gridWidth / 2 - nameMetrics.stringWidth(chord.name()) / 2, top - 14);

        paintGrid(g, x, top, gridWidth, stringCount);
        paintOpenAndMutedMarks(g, chord, x, top, stringCount);
        paintFingerDots(g, chord, x, top, stringCount);
        if (chord.baseFret() > 1) {
            g.setFont(ScoreFonts.CHORD_FRET_FONT);
            g.drawString(chord.baseFret() + "fr", x + gridWidth + 3, top + FRET_GAP);
        }
    }

    private static void paintGrid(Graphics2D g, int x, int top, int gridWidth, int stringCount) {
        g.setColor(ScoreColors.INK);
        g.setStroke(new BasicStroke(2f));
        g.draw(new Line2D.Double(x, top, x + gridWidth, top));
        g.setStroke(new BasicStroke(1f));
        for (int fret = 1; fret <= VISIBLE_FRETS; fret++) {
            int y = top + fret * FRET_GAP;
            g.draw(new Line2D.Double(x, y, x + gridWidth, y));
        }
        for (int string = 0; string < stringCount; string++) {
            int sx = x + string * STRING_GAP;
            g.draw(new Line2D.Double(sx, top, sx, top + VISIBLE_FRETS * FRET_GAP));
        }
    }

    private static int xOfString(int x, int stringCount, int string) {
        return x + (stringCount - string) * STRING_GAP;
    }

    private static void paintOpenAndMutedMarks(Graphics2D g, ChordDiagram chord, int x, int top, int stringCount) {
        g.setColor(ScoreColors.INK);
        g.setStroke(new BasicStroke(1f));
        for (int string = 1; string <= stringCount; string++) {
            int sx = xOfString(x, stringCount, string);
            if (chord.isOpen(string)) {
                g.draw(new Ellipse2D.Double(sx - 2.2, top - 8, 4.4, 4.4));
            } else if (!chord.isPlayed(string)) {
                g.draw(new Line2D.Double(sx - 2, top - 8, sx + 2, top - 4));
                g.draw(new Line2D.Double(sx - 2, top - 4, sx + 2, top - 8));
            }
        }
    }

    private static void paintFingerDots(Graphics2D g, ChordDiagram chord, int x, int top, int stringCount) {
        g.setColor(ScoreColors.INK);
        for (int string = 1; string <= stringCount; string++) {
            int fret = chord.fretOfString(string);
            if (fret <= 0) {
                continue;
            }
            int relativeFret = fret - chord.baseFret() + 1;
            if (relativeFret < 1 || relativeFret > VISIBLE_FRETS) {
                continue;
            }
            int sx = xOfString(x, stringCount, string);
            int fy = top + (relativeFret - 1) * FRET_GAP + FRET_GAP / 2;
            g.fill(new Ellipse2D.Double(sx - 2.6, fy - 2.6, 5.2, 5.2));
        }
    }
}
