package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.model.effects.BendPoint;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * La grilla clicable de un bend o una palanca. Toda la decision vive en
 * BendCurveEditor; esto solo traduce pixeles a posiciones de la grilla y dibuja
 * la curva resultante.
 */
public final class BendGridPanel extends JComponent {

    private static final int MIN_QUARTER_TONES = -BendPoint.MAX_QUARTER_TONES;
    private static final int MAX_QUARTER_TONES = BendPoint.MAX_QUARTER_TONES;
    private static final int ROWS = MAX_QUARTER_TONES - MIN_QUARTER_TONES;

    private final BendCurveEditor editor;

    public BendGridPanel(BendCurveEditor editor) {
        this.editor = editor;
        setPreferredSize(new Dimension(360, 180));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                int position = positionOf(event.getX());
                int quarterTones = quarterTonesOf(event.getY());
                if (SwingUtilities.isRightMouseButton(event)) {
                    editor.addVibratoAt(position);
                } else {
                    editor.clickAt(position, quarterTones);
                }
                repaint();
            }
        });
    }

    private int positionOf(int x) {
        double fraction = clamp01(x / (double) getWidth());
        return (int) Math.round(fraction * BendPoint.LAST_POSITION);
    }

    private int quarterTonesOf(int y) {
        double fraction = clamp01(y / (double) getHeight());
        return (int) Math.round(MAX_QUARTER_TONES - fraction * ROWS);
    }

    private static double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(UIManager.getColor("TextField.background"));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(gridLineColor());
        int zeroY = yOf(0);
        g.drawLine(0, zeroY, getWidth(), zeroY);

        g.setColor(curveColor());
        var points = editor.points();
        for (int index = 0; index < points.size() - 1; index++) {
            var from = points.get(index);
            var to = points.get(index + 1);
            g.drawLine(xOf(from.position()), yOf(from.quarterTones()), xOf(to.position()), yOf(to.quarterTones()));
        }
        for (var point : points) {
            int x = xOf(point.position());
            int y = yOf(point.quarterTones());
            g.fillOval(x - 3, y - 3, 6, 6);
            if (point.vibrato() > 0) {
                g.drawOval(x - 5 - point.vibrato(), y - 5 - point.vibrato(), (5 + point.vibrato()) * 2, (5 + point.vibrato()) * 2);
            }
        }
    }

    private int xOf(int position) {
        return (int) Math.round(position / (double) BendPoint.LAST_POSITION * getWidth());
    }

    private int yOf(int quarterTones) {
        double fraction = (MAX_QUARTER_TONES - quarterTones) / (double) ROWS;
        return (int) Math.round(fraction * getHeight());
    }

    private Color gridLineColor() {
        Color base = UIManager.getColor("Separator.foreground");
        return base != null ? base : Color.GRAY;
    }

    private Color curveColor() {
        Color base = UIManager.getColor("Component.focusColor");
        return base != null ? base : Color.ORANGE;
    }
}
