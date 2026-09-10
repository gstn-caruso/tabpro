package com.gstncaruso.tabpro.ui.theme;

import java.awt.Color;

/**
 * El calculo de contraste que pide WCAG 2.1: luminancia relativa sobre sRGB linealizado y el
 * cociente {@code (L1+0.05)/(L2+0.05)} entre el mas claro y el mas oscuro de los dos colores.
 */
public final class Contrast {

    /** El minimo que pide la SC 1.4.3 (AA) para texto normal. */
    public static final double TEXT_MINIMUM_RATIO = 4.5;

    /** El minimo que pide la SC 1.4.11 para componentes de interfaz y trazos con significado. */
    public static final double GRAPHICAL_MINIMUM_RATIO = 3.0;

    private Contrast() {
    }

    public static double ratio(Color one, Color other) {
        double lighter = Math.max(relativeLuminance(one), relativeLuminance(other));
        double darker = Math.min(relativeLuminance(one), relativeLuminance(other));
        return (lighter + 0.05) / (darker + 0.05);
    }

    private static double relativeLuminance(Color color) {
        return 0.2126 * linearized(color.getRed())
                + 0.7152 * linearized(color.getGreen())
                + 0.0722 * linearized(color.getBlue());
    }

    private static double linearized(int channel) {
        double normalized = channel / 255.0;
        return normalized <= 0.03928
                ? normalized / 12.92
                : Math.pow((normalized + 0.055) / 1.055, 2.4);
    }
}
