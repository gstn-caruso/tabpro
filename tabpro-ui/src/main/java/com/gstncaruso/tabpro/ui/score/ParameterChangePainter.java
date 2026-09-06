package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.effects.ParameterChange;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.core.notation.VerticalStack;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/**
 * Los cambios de parametro que el manual deja insertar en medio de la
 * partitura. El cambio de tempo se escribe como lo escribe la musica, una negra
 * y su numero; todo lo demas —paneo, volumen, instrumento, efectos— no tiene
 * simbolo propio y se anuncia con el rectangulito rojo que describe el manual.
 */
final class ParameterChangePainter {

    private static final Font TEMPO_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 10);

    /** Cuanto se despegan del pentagrama, para no pisar las notas que asoman por arriba. */
    private static final int STAFF_CLEARANCE = 16;

    private static final int MARK_WIDTH = 9;
    private static final int MARK_HEIGHT = 5;
    private static final int QUARTER_NOTE_WIDTH = 8;
    private static final int ROW_HEIGHT = 12;
    private static final int ROW_GAP = 1;

    private ParameterChangePainter() {
    }

    static void paintMeasure(Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex) {
        Measure measure = track.measure(measureIndex);
        for (int beatIndex = 0; beatIndex < measure.beats().size(); beatIndex++) {
            ParameterChange change = measure.beat(beatIndex).effects().parameterChange();
            if (!change.isEmpty()) {
                paintBeat(g, layout, trackIndex, measureIndex, beatIndex, change);
            }
        }
    }

    private static void paintBeat(
            Graphics2D g, ScoreLayout layout, int trackIndex, int measureIndex, int beatIndex,
            ParameterChange change) {
        Rectangle bounds = layout.beatBounds(trackIndex, measureIndex, beatIndex);
        int centerX = bounds.x + bounds.width / 2;
        int bottom = layout.staffTop(trackIndex, measureIndex) - STAFF_CLEARANCE;

        VerticalStack stack = new VerticalStack(ROW_GAP);
        change.valueOf(SoundParameter.TEMPO).ifPresent(
                bpm -> paintTempo(g, bpm, centerX, bottom - stack.claim(ROW_HEIGHT)));
        if (changesSomethingWithoutASymbol(change)) {
            paintMark(g, centerX, bottom - stack.claim(ROW_HEIGHT));
        }
    }

    /** Si el cambio toca algo que la notacion musical no sabe escribir. */
    private static boolean changesSomethingWithoutASymbol(ParameterChange change) {
        for (SoundParameter parameter : SoundParameter.values()) {
            if (parameter != SoundParameter.TEMPO && change.changes(parameter)) {
                return true;
            }
        }
        return false;
    }

    private static void paintMark(Graphics2D g, int centerX, int baseY) {
        g.setColor(ScoreColors.PARAMETER_CHANGE);
        g.fillRect(centerX - MARK_WIDTH / 2, baseY - MARK_HEIGHT, MARK_WIDTH, MARK_HEIGHT);
    }

    private static void paintTempo(Graphics2D g, int bpm, int centerX, int baselineY) {
        String label = "= " + bpm;
        g.setFont(TEMPO_FONT);
        FontMetrics metrics = g.getFontMetrics();
        int left = centerX - (QUARTER_NOTE_WIDTH + metrics.stringWidth(label)) / 2;
        paintQuarterNote(g, left, baselineY);
        g.setColor(ScoreColors.INK);
        g.drawString(label, left + QUARTER_NOTE_WIDTH, baselineY);
    }

    /** La negra a la que se refiere el numero: cabeza rellena y plica para arriba. */
    private static void paintQuarterNote(Graphics2D g, int x, int baselineY) {
        g.setColor(ScoreColors.INK);
        g.fill(new Ellipse2D.Double(x, baselineY - 4, 5, 4));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Line2D.Double(x + 4.4, baselineY - 3, x + 4.4, baselineY - 10));
    }
}
