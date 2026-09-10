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
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;

public final class GlyphIcon implements Icon {

    private static final float MEASURING_FONT_SIZE = 1000f;
    private static final FontRenderContext MEASURING_CONTEXT = new FontRenderContext(null, true, true);

    private final int size;
    private final Shape content;

    public GlyphIcon(int size, String... rows) {
        if (rows.length == 0) {
            throw new IllegalArgumentException("GlyphIcon necesita al menos un renglón de glifos");
        }
        this.size = size;
        Font measuringFont = BravuraFont.base().deriveFont(MEASURING_FONT_SIZE);
        requireEveryGlyphDisplayable(measuringFont, rows);
        this.content = scaledAndCentered(stackedAreaOf(measuringFont, rows), size);
    }

    private GlyphIcon(int size, Area content) {
        this.size = size;
        this.content = scaledAndCentered(content, size);
    }

    public static Icon overlaid(int size, String... glyphs) {
        if (glyphs.length == 0) {
            throw new IllegalArgumentException("GlyphIcon necesita al menos un renglón de glifos");
        }
        Font measuringFont = BravuraFont.base().deriveFont(MEASURING_FONT_SIZE);
        requireEveryGlyphDisplayable(measuringFont, glyphs);
        return new GlyphIcon(size, overlaidAreaOf(measuringFont, glyphs));
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

    private static Area stackedAreaOf(Font font, String[] rows) {
        Area content = new Area();
        double cursorTop = 0;
        for (String row : rows) {
            GlyphVector glyphs = font.createGlyphVector(MEASURING_CONTEXT, row);
            Rectangle2D rowBounds = glyphs.getVisualBounds();
            double placement = cursorTop - rowBounds.getMinY();
            content.add(new Area(glyphs.getOutline(0, (float) placement)));
            cursorTop += rowBounds.getHeight();
        }
        return content;
    }

    private static Area overlaidAreaOf(Font font, String[] glyphs) {
        Area content = new Area();
        for (String glyph : glyphs) {
            GlyphVector glyphVector = font.createGlyphVector(MEASURING_CONTEXT, glyph);
            content.add(new Area(glyphVector.getOutline(0, 0)));
        }
        return content;
    }

    private static Shape scaledAndCentered(Area content, int size) {
        Rectangle2D bounds = content.getBounds2D();
        double scale = size / Math.max(bounds.getWidth(), bounds.getHeight());
        AffineTransform centering = new AffineTransform();
        centering.translate(size / 2.0, size / 2.0);
        centering.scale(scale, scale);
        centering.translate(-bounds.getCenterX(), -bounds.getCenterY());
        return centering.createTransformedShape(content);
    }
}
