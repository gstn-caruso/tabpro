package com.gstncaruso.tabpro.ui.harmony;

import java.util.List;
import javax.swing.JComponent;

/**
 * El diagrama de grados de la escala elegida: el nombre de cada nota y su intervalo respecto
 * de la tonica, en columnas parejas, tal como lo dibuja el manual.
 */
public final class ScaleDegreesView extends JComponent {

    private List<Object> tones = List.of();

    public int degreeCount() {
        return tones.size();
    }
}
