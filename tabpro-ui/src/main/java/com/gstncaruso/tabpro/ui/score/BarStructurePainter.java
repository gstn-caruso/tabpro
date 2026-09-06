package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.bars.DirectionJump;
import com.gstncaruso.tabpro.core.model.bars.DirectionSymbol;
import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.core.notation.Clef;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.stream.Collectors;

/**
 * La estructura del compas: armadura y compas en cada cambio, barras de repeticion, finales
 * alternativos, doble barra, y los carteles que valen para toda la partitura (direcciones,
 * saltos y marcadores), que se dibujan una sola vez arriba del sistema.
 */
final class BarStructurePainter {

    private static final Font MARK_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 11);
    private static final Font SMALL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
    private static final Font REPEAT_COUNT_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 10);

    private BarStructurePainter() {
    }

    /** Lo que se dibuja sobre el pentagrama de cada pista: armadura, compas y repeticiones. */
    static void paintPerTrack(
            Graphics2D g, ScoreLayout layout, Track track, Clef clef, int trackIndex, int measureIndex) {
        Measure measure = track.measure(measureIndex);
        MeasureAttributes attributes = measure.attributes();

        if (layout.startsASystem(measureIndex)) {
            double keyX = layout.measureX(measureIndex) + 22;
            StaffPainter.paintKeySignature(g, layout, clef, attributes.keySignature(), trackIndex, measureIndex, keyX);
        } else if (layout.hasSignatureChange(measureIndex)) {
            double x = layout.measureX(measureIndex) + 6;
            KeySignature previousKey = track.measure(measureIndex - 1).attributes().keySignature();
            if (!attributes.keySignature().equals(previousKey)) {
                StaffPainter.paintKeySignature(g, layout, clef, attributes.keySignature(), trackIndex, measureIndex, x);
                x += attributes.keySignature().alteredCount() * ScoreLayout.STAFF_LINE_SPACING;
            }
            if (!measure.timeSignature().equals(track.measure(measureIndex - 1).timeSignature())) {
                StaffPainter.paintTimeSignature(g, layout, track, trackIndex, measureIndex, x);
            }
        }

        int left = layout.measureX(measureIndex);
        int right = left + layout.measureWidth(measureIndex);
        int top = layout.staffTop(trackIndex, measureIndex);
        int bottom = layout.tabBottom(trackIndex, measureIndex);

        if (attributes.repeatOpen()) {
            paintRepeatBracket(g, left, top, bottom, true);
        }
        if (attributes.repeatCloses()) {
            paintRepeatBracket(g, right, top, bottom, false);
            paintRepeatCount(g, right, top, attributes.repeatCount());
        }
        if (attributes.doubleBar()) {
            paintDoubleBar(g, right, top, bottom);
        }
    }

    /**
     * Lo que vale para toda la partitura y se dibuja una unica vez, arriba del primer pentagrama
     * del sistema: finales alternativos, direcciones, saltos y marcadores.
     *
     * <p>Limitacion conocida: comparten la misma franja de aire reservada para la clave y la
     * etiqueta de la pista (STAFF_HEADROOM); si un compas de arranque de sistema trae ademas un
     * marcador o una direccion, pueden superponerse visualmente. Resolverlo del todo pide que el
     * layout reserve una franja de altura variable segun el contenido, que queda fuera de esta
     * pasada.
     */
    static void paintScoreWide(Graphics2D g, ScoreLayout layout, Track track, int trackIndex, int measureIndex) {
        MeasureAttributes attributes = track.measure(measureIndex).attributes();
        int left = layout.measureX(measureIndex);
        int right = left + layout.measureWidth(measureIndex);
        int staffTop = layout.staffTop(trackIndex, measureIndex);

        if (attributes.hasAlternateEndings()) {
            paintAlternateEnding(g, left, right, staffTop, attributes.alternateEndings());
        }
        attributes.marker().ifPresent(marker -> paintMarker(g, left, staffTop, marker));
        attributes.symbol().ifPresent(symbol -> paintDirectionSymbol(g, left, right, staffTop, symbol));
        attributes.jump().ifPresent(jump -> paintJump(g, left, right, staffTop, jump));
    }

    private static void paintRepeatBracket(Graphics2D g, int x, int top, int bottom, boolean opening) {
        g.setColor(ScoreColors.INK);
        g.setStroke(new BasicStroke(2.4f));
        int barX = opening ? x + 3 : x - 3;
        g.draw(new Line2D.Double(barX, top, barX, bottom));
        g.setStroke(new BasicStroke(1f));
        int thinX = opening ? x + 7 : x - 7;
        g.draw(new Line2D.Double(thinX, top, thinX, bottom));

        double middle = (top + bottom) / 2.0;
        int dotX = opening ? x + 11 : x - 11;
        fillDot(g, dotX, middle - 5);
        fillDot(g, dotX, middle + 5);
    }

    private static void paintRepeatCount(Graphics2D g, int rightEdge, int top, int times) {
        g.setColor(ScoreColors.LABEL);
        g.setFont(REPEAT_COUNT_FONT);
        String label = repeatLabel(times);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(label, rightEdge - metrics.stringWidth(label) - 12, top - 4);
    }

    /** El cartelito de la barra de repeticion, arriba a la derecha del compas que cierra. */
    static String repeatLabel(int times) {
        return "x" + (times + 1);
    }

    private static void paintDoubleBar(Graphics2D g, int x, int top, int bottom) {
        g.setColor(ScoreColors.BAR_LINE);
        g.setStroke(new BasicStroke(1f));
        g.draw(new Line2D.Double(x - 4, top, x - 4, bottom));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Line2D.Double(x, top, x, bottom));
    }

    private static void fillDot(Graphics2D g, double x, double y) {
        g.fill(new Ellipse2D.Double(x - 1.6, y - 1.6, 3.2, 3.2));
    }

    private static void paintAlternateEnding(Graphics2D g, int left, int right, int staffTop, List<Integer> passes) {
        int y = staffTop - 14;
        g.setColor(ScoreColors.INK);
        g.setStroke(new BasicStroke(1.3f));
        g.draw(new Line2D.Double(left, y + 8, left, y));
        g.draw(new Line2D.Double(left, y, right - 4, y));

        String label = passes.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ".";
        g.setFont(SMALL_FONT);
        g.drawString(label, left + 4, y - 2);
    }

    private static void paintMarker(Graphics2D g, int x, int staffTop, Marker marker) {
        g.setFont(MARK_FONT);
        g.setColor(ScoreColors.of(marker.color()));
        g.drawString(marker.name(), x, staffTop - 26);
    }

    private static void paintDirectionSymbol(Graphics2D g, int left, int right, int staffTop, DirectionSymbol symbol) {
        int centerX = (left + right) / 2;
        int y = staffTop - 12;
        switch (symbol) {
            case CODA, DOUBLE_CODA -> paintCodaGlyph(g, centerX, y - 4, symbol == DirectionSymbol.DOUBLE_CODA);
            default -> paintCenteredLabel(g, symbol.label(), centerX, y);
        }
    }

    private static void paintCodaGlyph(Graphics2D g, int centerX, int y, boolean doubled) {
        g.setColor(ScoreColors.INK);
        g.setStroke(new BasicStroke(1.4f));
        paintOneCoda(g, centerX, y);
        if (doubled) {
            paintOneCoda(g, centerX - 10, y);
            paintOneCoda(g, centerX + 10, y);
        }
    }

    private static void paintOneCoda(Graphics2D g, int centerX, int y) {
        int radius = 5;
        g.draw(new Ellipse2D.Double(centerX - radius, y - radius, radius * 2, radius * 2));
        g.draw(new Line2D.Double(centerX, y - radius - 3, centerX, y + radius + 3));
        g.draw(new Line2D.Double(centerX - radius - 3, y, centerX + radius + 3, y));
    }

    private static void paintJump(Graphics2D g, int left, int right, int staffTop, DirectionJump jump) {
        paintCenteredLabel(g, jump.label(), (left + right) / 2, staffTop - 12);
    }

    private static void paintCenteredLabel(Graphics2D g, String text, int centerX, int y) {
        g.setColor(ScoreColors.INK);
        g.setFont(MARK_FONT);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, y);
    }
}
