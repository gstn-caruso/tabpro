package com.gstncaruso.tabpro.ui.theme;

import java.awt.Color;

/**
 * The contrast calculation WCAG 2.1 requires: relative luminance over linearized sRGB and the
 * ratio {@code (L1+0.05)/(L2+0.05)} between the lighter and darker of the two colors.
 */
public final class Contrast {

    /** The minimum SC 1.4.3 (AA) requires for normal text. */
    public static final double TEXT_MINIMUM_RATIO = 4.5;

    /** The minimum SC 1.4.11 requires for UI components and meaningful strokes. */
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
