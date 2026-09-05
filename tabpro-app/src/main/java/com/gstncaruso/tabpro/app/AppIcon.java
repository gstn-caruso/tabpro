package com.gstncaruso.tabpro.app;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/** El icono de la aplicacion: unas cuerdas con tres trastes escritos. */
final class AppIcon {

    private static final Color BACKGROUND = new Color(0x1E1F22);
    private static final Color STRINGS = new Color(0x8B8F96);
    private static final Color DIGITS = new Color(0xE8A33D);
    private static final List<int[]> FRETS = List.of(new int[] {5, 0}, new int[] {7, 2}, new int[] {0, 4});

    private AppIcon() {
    }

    static List<java.awt.Image> sizes() {
        return List.of(draw(16), draw(32), draw(64), draw(128));
    }

    private static BufferedImage draw(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(BACKGROUND);
        graphics.fill(new RoundRectangle2D.Double(0, 0, size, size, size * 0.2, size * 0.2));

        double top = size * 0.28;
        double spacing = size * 0.125;
        graphics.setColor(STRINGS);
        graphics.setStroke(new java.awt.BasicStroke(Math.max(1f, size / 42f)));
        for (int string = 0; string < 5; string++) {
            double y = top + string * spacing;
            graphics.draw(new Line2D.Double(size * 0.18, y, size * 0.82, y));
        }

        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(6, Math.round(size * 0.22f))));
        for (int index = 0; index < FRETS.size(); index++) {
            int[] fret = FRETS.get(index);
            double x = size * (0.26 + index * 0.22);
            double y = top + fret[1] * spacing;
            String text = String.valueOf(fret[0]);
            int width = graphics.getFontMetrics().stringWidth(text);
            graphics.setColor(BACKGROUND);
            graphics.fillRect((int) (x - width * 0.4), (int) (y - size * 0.13), (int) (width * 1.8), (int) (size * 0.26));
            graphics.setColor(DIGITS);
            graphics.drawString(text, (float) (x - width * 0.1), (float) (y + size * 0.08));
        }
        graphics.dispose();
        return image;
    }
}
