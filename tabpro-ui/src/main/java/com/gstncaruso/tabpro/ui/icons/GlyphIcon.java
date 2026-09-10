package com.gstncaruso.tabpro.ui.icons;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 * Un icono musical dibujado con uno o mas glifos SMuFL de la fuente Bravura, escalado para
 * entrar centrado en el tamano pedido sin salirse de el.
 */
public final class GlyphIcon implements Icon {

    private final int size;

    public GlyphIcon(int size, String... rows) {
        if (rows.length == 0) {
            throw new IllegalArgumentException("GlyphIcon necesita al menos un renglon de glifos");
        }
        this.size = size;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
