package com.gstncaruso.tabpro.ui.score;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Todos los pintores de la partitura dibujan tinta clara pensada para el fondo oscuro de la
 * pantalla. El Modo Pagina necesita esa misma tinta sobre una hoja clara: en vez de duplicar cada
 * pintor con una paleta distinta, se dibuja sobre un lienzo transparente (sin fondo) y se invierte
 * el color de cada pixel que no sea transparente. Tinta clara invertida da tinta oscura; lo que no
 * se toco queda transparente y deja ver la hoja de papel.
 */
final class PaperRenderer {

    private PaperRenderer() {
    }

    static BufferedImage renderAsInk(int width, int height, Consumer<Graphics2D> painting) {
        BufferedImage image = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        painting.accept(g);
        g.dispose();
        invert(image);
        return image;
    }

    private static void invert(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            int alpha = argb >>> 24;
            if (alpha == 0) {
                continue;
            }
            int red = 255 - ((argb >> 16) & 0xFF);
            int green = 255 - ((argb >> 8) & 0xFF);
            int blue = 255 - (argb & 0xFF);
            pixels[i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
        }
        image.setRGB(0, 0, width, height, pixels, 0, width);
    }
}
