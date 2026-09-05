package com.gstncaruso.tabpro.ui.instruments;

import java.awt.Graphics2D;

/** La forma de los marcadores de traste, que cambia segun el tipo de diapason. */
public enum InlayStyle {
    /** La clasica no lleva marcadores: el mastil queda liso. */
    NONE {
        @Override
        public void draw(Graphics2D g, int centerX, int centerY, int radius) {
            // sin marcador
        }
    },
    DOTS {
        @Override
        public void draw(Graphics2D g, int centerX, int centerY, int radius) {
            g.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }
    },
    DIAMONDS {
        @Override
        public void draw(Graphics2D g, int centerX, int centerY, int radius) {
            int[] xs = {centerX, centerX + radius, centerX, centerX - radius};
            int[] ys = {centerY - radius, centerY, centerY + radius, centerY};
            g.fillPolygon(xs, ys, 4);
        }
    };

    public abstract void draw(Graphics2D g, int centerX, int centerY, int radius);
}
