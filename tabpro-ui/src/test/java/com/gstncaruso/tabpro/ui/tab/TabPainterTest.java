package com.gstncaruso.tabpro.ui.tab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TabPainterTest {

    @Test
    void paintsTheBackground() {
        Track track = Track.standardGuitar("Guitarra");
        TabLayout layout = TabLayout.of(track, 800);
        BufferedImage image = new BufferedImage(800, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = graphicsOf(image);

        TabPainter.paint(g, layout, track, new Cursor(0, 0, 0, 1), Optional.empty());

        assertEquals(TabPainter.BACKGROUND.getRGB(), image.getRGB(0, 0));
    }

    private Graphics2D graphicsOf(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setClip(0, 0, image.getWidth(), image.getHeight());
        return g;
    }
}
