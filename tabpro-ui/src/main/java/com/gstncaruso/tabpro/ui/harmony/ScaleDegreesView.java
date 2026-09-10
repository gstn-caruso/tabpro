package com.gstncaruso.tabpro.ui.harmony;

import com.gstncaruso.tabpro.core.harmony.ScaleTone;
import java.util.List;
import javax.swing.JComponent;

/**
 * El diagrama de grados de la escala elegida: el nombre de cada nota y su intervalo respecto
 * de la tonica, en columnas parejas, tal como lo dibuja el manual.
 */
public final class ScaleDegreesView extends JComponent {

    private List<ScaleTone> tones = List.of();

    public void show(List<ScaleTone> tones) {
        this.tones = List.copyOf(tones);
        repaint();
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
}
