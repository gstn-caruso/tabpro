package com.gstncaruso.tabpro.ui.tracks;

import com.gstncaruso.tabpro.ui.a11y.AccessibleControl;
import java.awt.Color;
import javax.swing.JComponent;

/**
 * Un parametro de sonido dibujado como deslizador horizontal, tal como el volumen y el paneo en
 * la mesa de mezcla de Guitar Pro: una barra que se rellena segun el nivel y una caja numerica
 * que viaja sobre el cursor. Cuando el color de relleno y el de la parte vacia coinciden, la
 * barra se ve pintada de punta a punta y solo se mueve la caja -asi se lee el paneo, centrado en
 * el medio del rango.
 */
public final class LevelSlider extends JComponent implements AccessibleControl {

    private final int min;
    private final int max;
    private final Color fillColor;
    private final Color trackColor;
    private int value;
    private Runnable onUserChange = () -> {
    };

    public LevelSlider(int min, int max, int value, Color fillColor, Color trackColor) {
        this.min = min;
        this.max = max;
        this.value = clamp(value);
        this.fillColor = fillColor;
        this.trackColor = trackColor;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        int clamped = clamp(value);
        if (clamped != this.value) {
            this.value = clamped;
            repaint();
        }
    }

    public void onUserChange(Runnable listener) {
        this.onUserChange = listener;
    }

    private int clamp(int candidate) {
        return Math.max(min, Math.min(max, candidate));
    }
}
