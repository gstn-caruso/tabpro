package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.bars.Marker;
import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 * La franja arriba de la grilla de compases: el nombre de cada marcador con su color, sobre los
 * compases que abarca. Doble clic crea un marcador nuevo, o edita el que ya esta ahi.
 */
public final class MarkerZone extends JComponent {

    public static final int HEIGHT = 16;

    private final Editor editor;

    public MarkerZone(Editor editor) {
        this.editor = editor;
        setOpaque(true);
        setBackground(ScoreColors.SURFACE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editMarkerAt(measureAt(e.getX()));
                }
            }
        });
    }

    public int measureAt(int x) {
        int measure = x / MeasureGrid.CELL_WIDTH;
        return Math.max(0, Math.min(measure, Math.max(0, editor.score().measureCount() - 1)));
    }

    @Override
    public Dimension getPreferredSize() {
        Score score = editor.score();
        return new Dimension(Math.max(1, score.measureCount()) * MeasureGrid.CELL_WIDTH, HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(ScoreColors.SURFACE);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
        for (MarkerSegments.Segment segment : MarkerSegments.of(editor.score())) {
            paintSegment(g, segment);
        }
    }

    private void paintSegment(Graphics2D g, MarkerSegments.Segment segment) {
        Rectangle bounds = new Rectangle(
                segment.fromMeasure() * MeasureGrid.CELL_WIDTH,
                0,
                (segment.toMeasureExclusive() - segment.fromMeasure()) * MeasureGrid.CELL_WIDTH,
                HEIGHT);
        Color color = colorOf(segment.marker());
        g.setColor(color);
        g.fillRect(bounds.x, bounds.y + 1, bounds.width - 1, bounds.height - 2);
        g.setColor(readableInkOver(color));
        g.drawString(segment.marker().name(), bounds.x + 3, HEIGHT - 4);
    }

    private Color colorOf(Marker marker) {
        return new Color(marker.color().red(), marker.color().green(), marker.color().blue());
    }

    /** Texto negro o blanco segun que se lea mejor sobre el color del marcador. */
    private Color readableInkOver(Color background) {
        double brightness = (0.299 * background.getRed() + 0.587 * background.getGreen() + 0.114 * background.getBlue()) / 255;
        return brightness > 0.6 ? Color.BLACK : Color.WHITE;
    }

    private void editMarkerAt(int measureIndex) {
        Marker current = editor.score().attributesOf(measureIndex).marker().orElse(null);
        String initial = current == null ? "" : current.name();
        String chosen = JOptionPane.showInputDialog(this, "Nombre del marcador", initial);
        if (chosen == null || chosen.isBlank()) {
            return;
        }
        Marker marker = current == null ? Marker.named(chosen.trim()) : new Marker(chosen.trim(), current.color());
        moveEditorTo(measureIndex);
        editor.setMarker(marker);
    }

    private void moveEditorTo(int measureIndex) {
        if (measureIndex != editor.cursor().measure()) {
            editor.moveTo(measureIndex, 0, editor.cursor().string());
        }
    }
}
