package com.gstncaruso.tabpro.ui.print;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.ui.score.PageScorePainter;
import com.gstncaruso.tabpro.ui.score.ViewMode;
import com.gstncaruso.tabpro.ui.score.Zoom;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * La partitura dibujada fuera de la pantalla, tal como sale impresa o
 * exportada: en modo pagina, sin cursor ni seleccion.
 */
public final class ScoreSheets {

    /** Lo que mide una hoja en pixeles cuando se la dibuja a tamano natural. */
    public static final int DEFAULT_WIDTH = 900;

    private ScoreSheets() {
    }

    public static Dimension sizeOf(Score score, Zoom zoom) {
        return PageScorePainter.canvasSize(score, ViewMode.PAGE, zoom, DEFAULT_WIDTH);
    }

    /** Toda la partitura en una sola imagen, lista para guardar o para imprimir. */
    public static BufferedImage render(Score score, Zoom zoom) {
        Dimension size = sizeOf(score, zoom);
        BufferedImage sheet = new BufferedImage(
                Math.max(1, size.width), Math.max(1, size.height), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = sheet.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        paintOn(graphics, score, zoom);
        graphics.dispose();
        return sheet;
    }

    /** Dibuja la partitura sobre un lienzo ajeno, como el de la impresora. */
    public static void paintOn(Graphics2D graphics, Score score, Zoom zoom) {
        PageScorePainter.paint(
                graphics, score, hiddenCursor(), Playhead.silent(), Optional.empty(),
                ViewMode.PAGE, zoom, DEFAULT_WIDTH);
    }

    /** Un cursor que no se ve porque apunta a una pista que no existe. */
    private static Cursor hiddenCursor() {
        return new Cursor(-1, 0, 0, 1);
    }
}
