package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.ui.score.ScoreColors;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import javax.swing.JComponent;

/**
 * Un parametro de sonido dibujado como perilla giratoria, tal como lo describe el manual de
 * Guitar Pro para la mesa de mezcla. Barre 270 grados: abajo a la izquierda es el minimo, arriba
 * es la mitad, abajo a la derecha es el maximo.
 */
public final class Potentiometer extends JComponent {

    private static final double START_ANGLE = 225.0;
    private static final double SWEEP_DEGREES = -270.0;

    private final int min;
    private final int max;
    private int value;
    private Runnable onUserChange = () -> {
    };
    private int dragStartY;
    private int dragStartValue;

    public Potentiometer(int min, int max, int value) {
        this.min = min;
        this.max = max;
        this.value = clamp(value);
        setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        setToolTipText(String.valueOf(this.value));
        addMouseListener(dragStart());
        addMouseMotionListener(drag());
    }

    /** El angulo, en grados y en la convencion de Arc2D, que le corresponde a un valor. */
    public static double angleDegrees(int value, int min, int max) {
        double fraction = (value - min) / (double) (max - min);
        return START_ANGLE + fraction * SWEEP_DEGREES;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        int clamped = clamp(value);
        if (clamped != this.value) {
            this.value = clamped;
            setToolTipText(String.valueOf(this.value));
            repaint();
        }
    }

    public void onUserChange(Runnable listener) {
        this.onUserChange = listener;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(20, 20);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double size = Math.min(getWidth(), getHeight()) - 2;
        double x = (getWidth() - size) / 2.0;
        double y = (getHeight() - size) / 2.0;

        g.setColor(ScoreColors.MUTED_INK);
        g.draw(new Arc2D.Double(x, y, size, size, START_ANGLE, SWEEP_DEGREES, Arc2D.OPEN));

        g.setColor(ScoreColors.ACCENT);
        double swept = angleDegrees(value, min, max) - START_ANGLE;
        g.draw(new Arc2D.Double(x, y, size, size, START_ANGLE, swept, Arc2D.OPEN));

        double centerX = x + size / 2.0;
        double centerY = y + size / 2.0;
        double radius = size / 2.0;
        g.setColor(ScoreColors.SURFACE_HIGHLIGHT);
        g.fill(new Ellipse2D.Double(centerX - radius * 0.55, centerY - radius * 0.55, radius * 1.1, radius * 1.1));

        double angleRadians = Math.toRadians(angleDegrees(value, min, max));
        double needleX = centerX + Math.cos(angleRadians) * radius * 0.5;
        double needleY = centerY - Math.sin(angleRadians) * radius * 0.5;
        g.setColor(ScoreColors.INK);
        g.draw(new Line2D.Double(centerX, centerY, needleX, needleY));
    }

    private MouseAdapter dragStart() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStartY = e.getY();
                dragStartValue = value;
            }
        };
    }

    private MouseMotionAdapter drag() {
        return new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int delta = dragStartY - e.getY();
                setValue(dragStartValue + delta);
                onUserChange.run();
            }
        };
    }

    private int clamp(int candidate) {
        return Math.max(min, Math.min(max, candidate));
    }
}
