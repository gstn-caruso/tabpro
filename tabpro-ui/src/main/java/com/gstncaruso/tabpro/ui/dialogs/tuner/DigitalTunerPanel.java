package com.gstncaruso.tabpro.ui.dialogs.tuner;

import com.gstncaruso.tabpro.core.model.Pitch;
import com.gstncaruso.tabpro.core.notation.PitchName;
import com.gstncaruso.tabpro.ui.a11y.AccessibleControl;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Line2D;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.UIManager;

public final class DigitalTunerPanel extends JComponent implements AccessibleControl {

    public static final int MAX_CENTS = 50;

    private Pitch target;
    private int deviationCents;
    private boolean showsFocusRing;

    public DigitalTunerPanel(Pitch target) {
        this.target = target;
        setFocusable(true);
        setPreferredSize(new Dimension(220, 140));
        setToolTipText(Texts.get("score_dialogs.DigitalTunerPanel.title"));
        getAccessibleContext().setAccessibleName(Texts.get("score_dialogs.DigitalTunerPanel.title"));
        installFocusRing();
        updateAccessibleDescription();
    }

    private void updateAccessibleDescription() {
        getAccessibleContext().setAccessibleDescription(
                PitchName.of(target).textWithOctave() + ", " + deviationDescription());
    }

    private String deviationDescription() {
        if (deviationCents == 0) {
            return Texts.get("score_dialogs.DigitalTunerPanel.inTune");
        }
        String direction = deviationCents > 0
                ? Texts.get("score_dialogs.DigitalTunerPanel.sharp")
                : Texts.get("score_dialogs.DigitalTunerPanel.flat");
        return Texts.get("score_dialogs.DigitalTunerPanel.deviation", Math.abs(deviationCents), direction);
    }

    private void installFocusRing() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                showsFocusRing = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                showsFocusRing = false;
                repaint();
            }
        });
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.CANVAS;
                }
            };
        }
        return accessibleContext;
    }

    public void setTarget(Pitch target) {
        this.target = target;
        updateAccessibleDescription();
        repaint();
    }

    public Pitch target() {
        return target;
    }

    public void setDeviationCents(int cents) {
        this.deviationCents = Math.clamp(cents, -MAX_CENTS, MAX_CENTS);
        updateAccessibleDescription();
        repaint();
    }

    public int deviationCents() {
        return deviationCents;
    }

    public boolean isInTune() {
        return Math.abs(deviationCents) <= 3;
    }

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

        if (showsFocusRing) {
            paintFocusRing(g);
        }
    }

    private void paintFocusRing(Graphics2D g) {
        g.setColor(focusRingColor());
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

    private java.awt.Color focusRingColor() {
        java.awt.Color fromLookAndFeel = UIManager.getColor("Component.focusColor");
        return fromLookAndFeel != null ? fromLookAndFeel : java.awt.Color.ORANGE;
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
