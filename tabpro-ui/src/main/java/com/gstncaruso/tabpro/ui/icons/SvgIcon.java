package com.gstncaruso.tabpro.ui.icons;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import java.awt.Component;
import java.awt.Graphics;
import java.net.URL;
import javax.swing.Icon;

/** Un icono vectorial cargado desde un SVG del classpath, escalado al tamano pedido. */
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
