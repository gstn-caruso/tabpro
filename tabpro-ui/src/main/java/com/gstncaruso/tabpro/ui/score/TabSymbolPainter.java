package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.effects.BeatEffects;
import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.core.model.effects.Ornament;
import com.gstncaruso.tabpro.core.model.effects.PickstrokeDirection;
import com.gstncaruso.tabpro.core.notation.VerticalStack;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Los simbolos que el manual agrupa en "Add Symbols" y que se dibujan arriba de la tablatura:
 * PM, let ring, tapping, slap, pop, armonicos, vibrato, trino, tremolo de pua, rasgueo, pua y
 * texto libre. Se apilan con {@link VerticalStack} para no pisarse cuando coinciden varios en el
 * mismo beat.
 */
final class TabSymbolPainter {

    private static final Font SYMBOL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 9);
    private static final Font TEXT_FONT = new Font(Font.SANS_SERIF, Font.ITALIC, 10);
    private static final int ROW_HEIGHT = 11;
    private static final int ROW_GAP = 1;

    private TabSymbolPainter() {
    }

    static void paintMeasure(Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex) {
        Measure measure = track.measure(measureIndex);
        for (int beatIndex = 0; beatIndex < measure.beats().size(); beatIndex++) {
            paintBeat(g, layout, trackIndex, measureIndex, beatIndex, measure.beat(beatIndex));
        }
    }

    private static void paintBeat(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex, Beat beat) {
        Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, beatIndex);
        int centerX = bounds.x + bounds.width / 2;
        int tabTop = layout.tabTop(trackIndex, measureIndex);

        VerticalStack stack = new VerticalStack(ROW_GAP);
        for (String label : labelsFor(beat)) {
            int offset = stack.claim(ROW_HEIGHT);
            paintLabel(g, label, centerX, tabTop - 3 - offset);
        }
        beat.effects().stroke().ifPresent(stroke -> {
            int offset = stack.claim(ROW_HEIGHT);
            paintStrumArrow(g, centerX, tabTop - 3 - offset, stroke.direction().startsAtTheLowestString(), stroke.rasgueado());
        });
        beat.effects().pickstroke().ifPresent(direction -> {
            int offset = stack.claim(ROW_HEIGHT);
            paintPickstroke(g, centerX, tabTop - 3 - offset, direction);
        });
        beat.effects().text().ifPresent(text -> {
            int offset = stack.claim(ROW_HEIGHT);
            paintFreeText(g, text, centerX, tabTop - 3 - offset);
        });
    }

    /** Las siglas que valen una vez por beat, juntando lo que pida cualquiera de sus notas. */
    private static List<String> labelsFor(Beat beat) {
        List<String> labels = new ArrayList<>();
        BeatEffects effects = beat.effects();
        if (effects.tapping()) {
            labels.add("T");
        }
        if (effects.slapping()) {
            labels.add("S");
        }
        if (effects.popping()) {
            labels.add("P");
        }
        if (has(beat, Ornament.PALM_MUTE)) {
            labels.add("P.M.");
        }
        if (has(beat, Ornament.LET_RING)) {
            labels.add("let ring");
        }
        harmonicOf(beat).ifPresent(harmonic -> labels.add(harmonic.symbol()));
        if (beat.notes().stream().anyMatch(note -> note.effects().trill().isPresent())) {
            labels.add("tr");
        }
        if (beat.notes().stream().anyMatch(note -> note.effects().tremoloPicking().isPresent())) {
            labels.add("ℇℇℇ");
        }
        if (effects.wideVibrato() || has(beat, Ornament.VIBRATO)) {
            labels.add(effects.wideVibrato() ? "⌇⌇" : "⌇");
        }
        return labels;
    }

    private static boolean has(Beat beat, Ornament ornament) {
        return beat.notes().stream().anyMatch(note -> note.has(ornament));
    }

    private static Optional<HarmonicType> harmonicOf(Beat beat) {
        return beat.notes().stream().flatMap(note -> note.effects().harmonic().stream()).findFirst();
    }

    private static void paintLabel(Graphics2D g, String text, int centerX, int baselineY) {
        g.setFont(text.length() > 3 ? TEXT_FONT : SYMBOL_FONT);
        g.setColor(ScoreColors.LABEL);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

    private static void paintFreeText(Graphics2D g, String text, int centerX, int baselineY) {
        g.setFont(TEXT_FONT);
        g.setColor(ScoreColors.INK);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }

    private static void paintStrumArrow(Graphics2D g, int centerX, int y, boolean downwards, boolean rasgueado) {
        g.setColor(ScoreColors.LABEL);
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int top = y - 8;
        int bottom = y;
        int arrowY = downwards ? bottom : top;
        int tipY = downwards ? top : bottom;
        g.draw(new Line2D.Double(centerX, top, centerX, bottom));
        GeneralPath head = new GeneralPath();
        head.moveTo(centerX - 3, arrowY + (downwards ? -3 : 3));
        head.lineTo(centerX, tipY);
        head.lineTo(centerX + 3, arrowY + (downwards ? -3 : 3));
        g.draw(head);
        if (rasgueado) {
            g.setFont(SYMBOL_FONT);
            g.drawString("R", centerX + 5, y - 2);
        }
    }

    private static void paintPickstroke(Graphics2D g, int centerX, int y, PickstrokeDirection direction) {
        g.setColor(ScoreColors.LABEL);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath path = new GeneralPath();
        if (direction == PickstrokeDirection.DOWN) {
            path.moveTo(centerX - 4, y - 8);
            path.lineTo(centerX - 4, y - 2);
            path.lineTo(centerX + 4, y - 2);
            path.lineTo(centerX + 4, y - 8);
        } else {
            path.moveTo(centerX - 4, y - 2);
            path.lineTo(centerX, y - 8);
            path.lineTo(centerX + 4, y - 2);
        }
        g.draw(path);
    }
}
