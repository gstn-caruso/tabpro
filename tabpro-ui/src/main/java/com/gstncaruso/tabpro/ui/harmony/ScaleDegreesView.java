package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.harmony.ScaleTone;
import com.gstncaruso.tabpro.ui.dialogs.style.Labels;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;

public final class ScaleDegreesView extends JComponent {

    public static final int PREFERRED_WIDTH = 420;
    public static final int PREFERRED_HEIGHT = 70;

    private static final int SIDE_MARGIN = 20;

    private List<ScaleTone> tones = List.of();

    public ScaleDegreesView() {
        setOpaque(true);
        setBackground(ChordDiagramColors.BACKGROUND);
        setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        setToolTipText("Grados de la escala");
        getAccessibleContext().setAccessibleName("Grados de la escala");
    }

    public void show(List<ScaleTone> tones) {
        this.tones = List.copyOf(tones);
        getAccessibleContext().setAccessibleDescription(describeDegrees());
        repaint();
    }

    private String describeDegrees() {
        StringBuilder description = new StringBuilder();
        for (int index = 0; index < degreeCount(); index++) {
            if (index > 0) {
                description.append(", ");
            }
            description.append(noteLabel(index)).append(' ').append(intervalLabel(index));
        }
        return description.toString();
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
            };
        }
        return accessibleContext;
    }

    public int degreeCount() {
        return tones.size();
    }

    public String noteLabel(int index) {
        return tones.get(index).pitchClass().name();
    }

    public String intervalLabel(int index) {
        return Labels.of(tones.get(index).interval());
    }

    public int degreeX(int index) {
        return SIDE_MARGIN + (int) Math.round(index * columnGap());
    }

    private double columnGap() {
        return degreeCount() <= 1 ? 0 : (double) (getWidth() - 2 * SIDE_MARGIN) / (degreeCount() - 1);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(ChordDiagramColors.BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());

        int noteY = getHeight() / 2 - 6;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        FontMetrics noteMetrics = g.getFontMetrics();
        g.setColor(ChordDiagramColors.NUT);
        for (int index = 0; index < degreeCount(); index++) {
            String label = noteLabel(index);
            g.drawString(label, degreeX(index) - noteMetrics.stringWidth(label) / 2, noteY);
        }

        int intervalY = getHeight() / 2 + 18;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        FontMetrics intervalMetrics = g.getFontMetrics();
        g.setColor(ChordDiagramColors.LABEL);
        for (int index = 0; index < degreeCount(); index++) {
            String label = intervalLabel(index);
            g.drawString(label, degreeX(index) - intervalMetrics.stringWidth(label) / 2, intervalY);
        }
    }
}
