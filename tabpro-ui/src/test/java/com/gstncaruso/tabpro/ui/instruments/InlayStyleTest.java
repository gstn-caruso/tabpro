package com.gstncaruso.tabpro.ui.instruments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class InlayStyleTest {

    @Test
    void noneDoesNotTouchTheCanvas() {
        BufferedImage image = blank();

        InlayStyle.NONE.draw(graphicsOf(image), 5, 5, 3);

        assertEquals(Color.BLACK.getRGB(), image.getRGB(5, 5));
    }

    @Test
    void dotsPaintTheCenter() {
        BufferedImage image = blank();

        InlayStyle.DOTS.draw(graphicsOf(image), 5, 5, 3);

        assertNotEquals(Color.BLACK.getRGB(), image.getRGB(5, 5));
    }

    @Test
    void diamondsPaintTheCenterButNotTheCorners() {
        BufferedImage image = blank();

        InlayStyle.DIAMONDS.draw(graphicsOf(image), 5, 5, 3);

        assertNotEquals(Color.BLACK.getRGB(), image.getRGB(5, 5));
        assertEquals(Color.BLACK.getRGB(), image.getRGB(1, 1), "el diamante no llena las esquinas");
    }

    private static BufferedImage blank() {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 10, 10);
        g.dispose();
        return image;
    }

    private static Graphics2D graphicsOf(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        return g;
    }
}
