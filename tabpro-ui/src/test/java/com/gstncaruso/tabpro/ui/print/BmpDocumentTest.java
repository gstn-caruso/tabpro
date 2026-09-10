package com.gstncaruso.tabpro.ui.print;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;
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

    @Test
    void elBmpEscritoEsIdenticoByteAByteAlDeImageIo() throws IOException {
        BufferedImage image = opaquePage(5, 3);

        assertArrayEquals(writeWithImageIo(image), writeWithBmpDocument(image));
    }

    @Test
    void writeToPideLosPixelesPorFilaNoUnoPorUno() throws IOException {
        PixelAccessCountingImage image = new PixelAccessCountingImage(9, 4);

        writeWithBmpDocument(image);

        assertEquals(0, image.singlePixelCalls(), "no puede llamar a getRGB(x, y) por cada pixel");
        assertEquals(image.getHeight(), image.bulkRowCalls(), "tiene que pedir los pixeles fila por fila");
    }

    private static byte[] writeWithBmpDocument(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BmpDocument.writeTo(image, out);
        return out.toByteArray();
    }

    private static byte[] writeWithImageIo(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(out)) {
            ImageIO.write(image, "bmp", ios);
        }
        return out.toByteArray();
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
