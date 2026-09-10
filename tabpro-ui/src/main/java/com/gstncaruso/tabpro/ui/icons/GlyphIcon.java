package com.gstncaruso.tabpro.ui.icons;

import com.gstncaruso.tabpro.ui.font.BravuraFont;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;

/**
 * Un icono musical dibujado con uno o mas glifos SMuFL de la fuente Bravura, escalado para
 * entrar centrado en el tamano pedido sin salirse de el. Cada renglon se apila debajo del
 * anterior; dentro de un mismo renglon los glifos se ubican uno al lado del otro.
 */
public final class GlyphIcon implements Icon {

    private static final float MEASURING_FONT_SIZE = 1000f;
    private static final FontRenderContext MEASURING_CONTEXT = new FontRenderContext(null, true, true);

    private final int size;
    private final Shape content;

    public GlyphIcon(int size, String... rows) {
        if (rows.length == 0) {
            throw new IllegalArgumentException("GlyphIcon necesita al menos un renglon de glifos");
        }
        this.size = size;
        Font measuringFont = BravuraFont.base().deriveFont(MEASURING_FONT_SIZE);
        requireEveryGlyphDisplayable(measuringFont, rows);
        this.content = centeredContentOf(measuringFont, rows, size);
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        canvas.translate(x, y);
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setColor(component == null ? canvas.getColor() : component.getForeground());
        canvas.fill(content);
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

    private static void requireEveryGlyphDisplayable(Font font, String[] rows) {
        for (String row : rows) {
            for (int index = 0; index < row.length(); index++) {
                char glyph = row.charAt(index);
                if (!font.canDisplay(glyph)) {
                    throw new IllegalArgumentException(
                            "Bravura no tiene el glifo U+%04X".formatted((int) glyph));
                }
            }
        }
    }

    private static Shape centeredContentOf(Font font, String[] rows, int size) {
        Area content = new Area();
        LineMetrics metrics = font.getLineMetrics(" ", MEASURING_CONTEXT);
        float lineHeight = metrics.getHeight();
        for (int row = 0; row < rows.length; row++) {
            GlyphVector glyphs = font.createGlyphVector(MEASURING_CONTEXT, rows[row]);
            content.add(new Area(glyphs.getOutline(0, row * lineHeight)));
        }
        Rectangle2D bounds = content.getBounds2D();
        double scale = size / Math.max(bounds.getWidth(), bounds.getHeight());
        AffineTransform centering = new AffineTransform();
        centering.translate(size / 2.0, size / 2.0);
        centering.scale(scale, scale);
        centering.translate(-bounds.getCenterX(), -bounds.getCenterY());
        return centering.createTransformedShape(content);
    }
}
