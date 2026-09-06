package com.gstncaruso.tabpro.ui.print;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.ui.page.PageMetrics;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.score.PageScorePainter;
import com.gstncaruso.tabpro.ui.score.ScoreViewport;
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

    private ScoreSheets() {
    }

    public static Dimension sizeOf(Score score, Zoom zoom, PageSetup setup) {
        return PageScorePainter.canvasSize(score, sheetViewport(zoom, setup));
    }

    /** Toda la partitura en una sola imagen, lista para guardar o para imprimir. */
    public static BufferedImage render(Score score, Zoom zoom, PageSetup setup) {
        Dimension size = sizeOf(score, zoom, setup);
        BufferedImage sheet = new BufferedImage(
                Math.max(1, size.width), Math.max(1, size.height), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = sheet.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        paintOn(graphics, score, zoom, setup);
        graphics.dispose();
        return sheet;
    }

    /**
     * La partitura repartida en hojas. En modo pagina las hojas se apilan una
     * debajo de la otra, separadas por un hueco, asi que alcanza con cortar.
     */
    public static java.util.List<BufferedImage> renderPages(Score score, Zoom zoom, PageSetup setup) {
        BufferedImage whole = render(score, zoom, setup);
        int pageHeight = (int) Math.round(PageMetrics.of(setup).pageHeight() * zoom.factor());
        int gap = (int) Math.round(PageMetrics.PAGE_GAP * zoom.factor());
        java.util.List<BufferedImage> sheets = new java.util.ArrayList<>();
        for (int top = 0; top < whole.getHeight(); top += pageHeight + gap) {
            int height = Math.min(pageHeight, whole.getHeight() - top);
            if (height <= 0) {
                break;
            }
            sheets.add(whole.getSubimage(0, top, whole.getWidth(), height));
        }
        return sheets.isEmpty() ? java.util.List.of(whole) : sheets;
    }

    /** Dibuja la partitura sobre un lienzo ajeno, como el de la impresora. */
    public static void paintOn(Graphics2D graphics, Score score, Zoom zoom, PageSetup setup) {
        PageScorePainter.paint(
                graphics, score, hiddenCursor(), Playhead.silent(), Optional.empty(), sheetViewport(zoom, setup));
    }

    /** La hoja impresa siempre lleva la partitura entera, con todas sus pistas. */
    private static ScoreViewport sheetViewport(Zoom zoom, PageSetup setup) {
        return ScoreViewport.of(ViewMode.PAGE, zoom, PageMetrics.of(setup).pageWidth()).withPageSetup(setup);
    }

    /** Un cursor que no se ve porque apunta a una pista que no existe. */
    private static Cursor hiddenCursor() {
        return new Cursor(-1, 0, 0, 1);
    }
}
