package com.gstncaruso.tabpro.ui.print;

import java.awt.image.BufferedImage;

/**
 * Un escritor de BMP mínimo, escrito a mano igual que {@link PdfDocument}: el formato clásico de
 * Windows (BITMAPFILEHEADER + BITMAPINFOHEADER, 24 bits por pixel, sin compresión), pidiendo los
 * pixeles en bloque en vez de uno por uno.
 */
final class BmpDocument {

    private BmpDocument() {
    }

    static boolean canEncode(BufferedImage image) {
        return !image.getColorModel().hasAlpha();
    }
}
