package com.gstncaruso.tabpro.ui.icons;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.ViewBox;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.net.URL;
import javax.swing.Icon;

public final class SvgIcon implements Icon {

    private final SVGDocument document;
    private final int size;

    public SvgIcon(String resourcePath, int size) {
        URL resource = SvgIcon.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalArgumentException("No existe el recurso SVG: " + resourcePath);
        }
        this.document = new SVGLoader().load(resource);
        this.size = size;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        canvas.translate(x, y);
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setColor(component == null ? canvas.getColor() : component.getForeground());
        document.render(component, canvas, new ViewBox(0, 0, size, size));
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
