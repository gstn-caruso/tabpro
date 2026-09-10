package com.gstncaruso.tabpro.ui.tracks;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.Track;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class TrackPanelAlignmentTest {

    private static final int WIDTH = 1300;
    private static final int HEIGHT = 180;
    private static final int STRIPE_WIDTH = 4;

    @Test
    void everyTrackSquareSitsAtTheHeightOfItsOwnRowInTheMixer() {
        TrackPanel panel = panelWithTwoTracksPlayingTheFirstMeasure();
        BufferedImage painted = paint(panel);

        for (int track = 0; track < 2; track++) {
            Band row = bandOf(painted, TrackColors.of(track), 0, STRIPE_WIDTH);
            Band square = bandOf(painted, TrackColors.of(track), MixTable.WIDTH, WIDTH);

            assertTrue(square.isInside(row),
                    "el cuadrado de la pista " + (track + 1) + " tiene que caer dentro de su fila del mixer:"
                            + " fila " + row + ", cuadrado " + square);
            assertTrue(Math.abs(row.center() - square.center()) <= 1,
                    "el cuadrado de la pista " + (track + 1) + " tiene que ir centrado en su fila del mixer:"
                            + " fila " + row + ", cuadrado " + square);
        }
    }

    private static TrackPanel panelWithTwoTracksPlayingTheFirstMeasure() {
        Editor editor = new Editor(Score.blank());
        editor.addTrack(Track.standardBass("Bajo"));
        for (int track = 0; track < 2; track++) {
            editor.selectTrack(track);
            editor.moveTo(0, 0, 1);
            editor.setFret(3);
        }
        editor.selectTrack(0);
        return new TrackPanel(editor);
    }

    private static BufferedImage paint(TrackPanel panel) {
        panel.setSize(WIDTH, HEIGHT);
        layOut(panel);
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        panel.paint(g);
        g.dispose();
        return image;
    }

    private static void layOut(Component component) {
        if (component instanceof Container container) {
            container.doLayout();
            for (Component child : container.getComponents()) {
                layOut(child);
            }
        }
    }

    private static Band bandOf(BufferedImage painted, Color color, int fromX, int toX) {
        int top = Integer.MAX_VALUE;
        int bottom = Integer.MIN_VALUE;
        for (int x = fromX; x < Math.min(toX, painted.getWidth()); x++) {
            for (int y = 0; y < painted.getHeight(); y++) {
                if (painted.getRGB(x, y) == color.getRGB()) {
                    top = Math.min(top, y);
                    bottom = Math.max(bottom, y);
                }
            }
        }
        if (bottom < top) {
            return fail("no se pinto ningun pixel " + color + " entre x=" + fromX + " y x=" + toX);
        }
        return new Band(top, bottom);
    }

    private record Band(int top, int bottom) {

        boolean isInside(Band other) {
            return top >= other.top && bottom <= other.bottom;
        }

        int center() {
            return (top + bottom) / 2;
        }

        @Override
        public String toString() {
            return "y=" + top + ".." + bottom;
        }
    }
}
