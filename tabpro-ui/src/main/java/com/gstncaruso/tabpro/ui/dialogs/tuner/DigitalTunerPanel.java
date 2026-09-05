package com.gstncaruso.tabpro.ui.dialogs.tuner;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.notation.PitchName;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * El afinador digital: una aguja que se mueve segun cuanto se aparta la nota
 * escuchada de la altura de referencia, en centesimas de semitono.
 */
public final class DigitalTunerPanel extends JComponent {

    public static final int MAX_CENTS = 50;

    private Pitch target;
    private int deviationCents;

    public DigitalTunerPanel(Pitch target) {
        this.target = target;
        setPreferredSize(new Dimension(220, 140));
    }

    public void setTarget(Pitch target) {
        this.target = target;
        repaint();
    }

    public Pitch target() {
        return target;
    }

    /** Cuantas centesimas de semitono esta desafinado: negativo grave, positivo agudo. */
    public void setDeviationCents(int cents) {
        this.deviationCents = Math.clamp(cents, -MAX_CENTS, MAX_CENTS);
        repaint();
    }

    public int deviationCents() {
        return deviationCents;
    }

    public boolean isInTune() {
        return Math.abs(deviationCents) <= 3;
    }

    /** El angulo de la aguja en radianes, de -60 a +60 grados. */
    static double needleAngleRadians(int cents) {
        double maxAngle = Math.toRadians(60);
        return (cents / (double) MAX_CENTS) * maxAngle;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int pivotX = width / 2;
        int pivotY = height - 10;
        int length = Math.min(width, height) - 20;

        g.setColor(textColor());
        g.drawString(PitchName.of(target).textWithOctave(), pivotX - 10, 16);

        g.setColor(isInTune() ? inTuneColor() : needleColor());
        double angle = needleAngleRadians(deviationCents);
        int tipX = pivotX + (int) Math.round(length * Math.sin(angle));
        int tipY = pivotY - (int) Math.round(length * Math.cos(angle));
        g.draw(new Line2D.Double(pivotX, pivotY, tipX, tipY));
    }

    private java.awt.Color textColor() {
        java.awt.Color color = UIManager.getColor("Label.foreground");
        return color != null ? color : java.awt.Color.WHITE;
    }

    private java.awt.Color needleColor() {
        java.awt.Color color = UIManager.getColor("Component.focusColor");
        return color != null ? color : java.awt.Color.ORANGE;
    }

    private java.awt.Color inTuneColor() {
        return new java.awt.Color(0x4CAF50);
    }
}
