package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.harmony.ScaleTone;
import java.util.List;
import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;

/**
 * El diagrama de grados de la escala elegida: el nombre de cada nota y su intervalo respecto
 * de la tonica, en columnas parejas, tal como lo dibuja el manual.
 */
public final class ScaleDegreesView extends JComponent {

    private static final int SIDE_MARGIN = 20;

    private List<ScaleTone> tones = List.of();

    public ScaleDegreesView() {
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
        return tones.get(index).interval().label();
    }

    /** La posicion horizontal de la columna de ese grado, parejas entre si como en el manual. */
    public int degreeX(int index) {
        return SIDE_MARGIN + (int) Math.round(index * columnGap());
    }

    private double columnGap() {
        return degreeCount() <= 1 ? 0 : (double) (getWidth() - 2 * SIDE_MARGIN) / (degreeCount() - 1);
    }
}
