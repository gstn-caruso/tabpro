package com.gstncaruso.tabpro.ui.icons;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/** Un icono que se dibuja con Java2D en vez de venir de un archivo. */
public final class ToolIcon implements Icon {

    /** Como se dibuja el icono, ya centrado y con el color de la interfaz puesto. */
    @FunctionalInterface
    public interface Drawing {
        void draw(Graphics2D graphics, int size);
    }

    private final Drawing drawing;
    private final int size;

    public ToolIcon(int size, Drawing drawing) {
        this.size = size;
        this.drawing = drawing;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D canvas = (Graphics2D) graphics.create(x, y, size, size);
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        canvas.setColor(component == null ? canvas.getColor() : component.getForeground());
        drawing.draw(canvas, size);
        canvas.dispose();
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
