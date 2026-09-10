package com.gstncaruso.tabpro.ui.print;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class BmpDocumentTest {

    @Test
    void unaImagenSinCanalAlfaSePuedeCodificar() {
        assertTrue(BmpDocument.canEncode(opaquePage(3, 2)));
    }

    @Test
    void unaImagenConTransparenciaRealNoSePuedeCodificar() {
        BufferedImage translucida = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        translucida.setRGB(0, 0, 0x80FF0000);

        assertFalse(BmpDocument.canEncode(translucida));
    }

    private static BufferedImage opaquePage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);
        graphics.drawLine(0, 0, width, height);
        graphics.dispose();
        return image;
    }
}
