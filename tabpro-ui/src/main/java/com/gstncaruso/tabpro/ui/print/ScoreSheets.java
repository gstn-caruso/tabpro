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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * La partitura dibujada fuera de la pantalla, tal como sale impresa o exportada: en modo pagina,
 * sobre el papel que pide la configuracion de pagina y sin cursor ni seleccion.
 */
public final class ScoreSheets {

    private ScoreSheets() {
    }

    public static Dimension sizeOf(Score score, Zoom zoom, PageSetup setup) {
        return PageScorePainter.canvasSize(score, sheetViewport(zoom, setup));
    }

    /** Lo que mide una sola hoja de papel dibujada con ese zoom. */
    public static Dimension pageSize(Zoom zoom, PageSetup setup) {
        PageMetrics sheet = PageMetrics.of(setup);
        return new Dimension(
                (int) Math.round(sheet.pageWidth() * zoom.factor()),
                (int) Math.round(sheet.pageHeight() * zoom.factor()));
    }

    /** En cuantas hojas se reparte la partitura con ese papel. */
    public static int pageCount(Score score, PageSetup setup) {
        return PageScorePainter.pageCount(score, sheetViewport(Zoom.whole(), setup));
    }

    /** Toda la partitura en una sola imagen, lista para guardar. */
    public static BufferedImage render(Score score, Zoom zoom, PageSetup setup) {
        Dimension size = sizeOf(score, zoom, setup);
        return drawnOn(size, graphics -> paintOn(graphics, score, zoom, setup));
    }

    /** Una hoja sola, del tamano exacto del papel. */
    public static BufferedImage renderPage(Score score, Zoom zoom, PageSetup setup, int page) {
        return drawnOn(pageSize(zoom, setup), graphics -> paintPageOn(graphics, score, zoom, setup, page));
    }

    /** La partitura repartida en hojas: una imagen por hoja, cada una del tamano del papel. */
    public static List<BufferedImage> renderPages(Score score, Zoom zoom, PageSetup setup) {
        List<BufferedImage> sheets = new ArrayList<>();
        for (int page = 0; page < pageCount(score, setup); page++) {
            sheets.add(renderPage(score, zoom, setup, page));
        }
        return List.copyOf(sheets);
    }

    /** Dibuja la partitura entera sobre un lienzo ajeno. */
    public static void paintOn(Graphics2D graphics, Score score, Zoom zoom, PageSetup setup) {
        PageScorePainter.paint(
                graphics, score, hiddenCursor(), Playhead.silent(), Optional.empty(), sheetViewport(zoom, setup));
    }

    /** Dibuja una hoja sola sobre un lienzo ajeno, como el de la impresora. */
    public static void paintPageOn(Graphics2D graphics, Score score, Zoom zoom, PageSetup setup, int page) {
        PageScorePainter.paintPage(
                graphics, score, hiddenCursor(), Playhead.silent(), Optional.empty(),
                sheetViewport(zoom, setup), page);
    }

    private static BufferedImage drawnOn(Dimension size, java.util.function.Consumer<Graphics2D> painting) {
        BufferedImage sheet = new BufferedImage(
                Math.max(1, size.width), Math.max(1, size.height), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = sheet.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        painting.accept(graphics);
        graphics.dispose();
        return sheet;
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
