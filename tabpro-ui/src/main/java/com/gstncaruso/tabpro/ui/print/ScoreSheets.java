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

    /**
     * La hoja se dibuja sin canal alfa a proposito. La exportacion a BMP no sabe escribir
     * transparencia y no avisa a los gritos: {@code ImageIO.write} devuelve false, no tira ninguna
     * excepcion y no deja ningun archivo. Una hoja opaca es papel de verdad, ademas: lo que no se
     * dibujo es blanco, no un agujero.
     */
    private static final int OPAQUE_PAPER = BufferedImage.TYPE_INT_RGB;

    private ScoreSheets() {
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

    /**
     * Toda la partitura en una sola imagen, lista para guardar, tal como se ve en la ventana: con
     * el modo de vista y el zoom que tiene puestos quien exporta -pagina o pergamino paginan
     * distinto, y el zoom cambia el tamano en pixeles.
     */
    public static BufferedImage render(Score score, ViewMode viewMode, Zoom zoom, PageSetup setup) {
        ScoreViewport viewport = viewportFor(viewMode, zoom, setup);
        Dimension size = PageScorePainter.canvasSize(score, viewport);
        return drawnOn(size, graphics -> paintOn(graphics, score, viewport));
    }

    /** Igual que el de arriba, pero siempre en modo Pagina: lo que usan imprimir y el PDF. */
    public static BufferedImage render(Score score, Zoom zoom, PageSetup setup) {
        return render(score, ViewMode.PAGE, zoom, setup);
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

    /** Dibuja la partitura entera sobre un lienzo ajeno, tal como se ve en la ventana. */
    public static void paintOn(Graphics2D graphics, Score score, ViewMode viewMode, Zoom zoom, PageSetup setup) {
        paintOn(graphics, score, viewportFor(viewMode, zoom, setup));
    }

    /** Igual que el de arriba, pero siempre en modo Pagina. */
    public static void paintOn(Graphics2D graphics, Score score, Zoom zoom, PageSetup setup) {
        paintOn(graphics, score, ViewMode.PAGE, zoom, setup);
    }

    private static void paintOn(Graphics2D graphics, Score score, ScoreViewport viewport) {
        PageScorePainter.paint(
                graphics, score, hiddenCursor(), Playhead.silent(), Optional.empty(), viewport);
    }

    /** Dibuja una hoja sola sobre un lienzo ajeno, como el de la impresora. */
    public static void paintPageOn(Graphics2D graphics, Score score, Zoom zoom, PageSetup setup, int page) {
        PageScorePainter.paintPage(
                graphics, score, hiddenCursor(), Playhead.silent(), Optional.empty(),
                sheetViewport(zoom, setup), page);
    }

    private static BufferedImage drawnOn(Dimension size, java.util.function.Consumer<Graphics2D> painting) {
        BufferedImage sheet = new BufferedImage(
                Math.max(1, size.width), Math.max(1, size.height), OPAQUE_PAPER);
        Graphics2D graphics = sheet.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        painting.accept(graphics);
        graphics.dispose();
        return sheet;
    }

    /**
     * La hoja impresa siempre lleva la partitura entera, con todas sus pistas, y siempre en modo
     * Pagina: imprimir y exportar a PDF reparten fisicamente en hojas sin importar que modo de
     * vista tiene la ventana -eso solo le importa a la exportacion de imagen.
     */
    private static ScoreViewport sheetViewport(Zoom zoom, PageSetup setup) {
        return viewportFor(ViewMode.PAGE, zoom, setup);
    }

    private static ScoreViewport viewportFor(ViewMode viewMode, Zoom zoom, PageSetup setup) {
        return ScoreViewport.of(viewMode, zoom, PageMetrics.of(setup).pageWidth()).withPageSetup(setup);
    }

    /** Un cursor que no se ve porque apunta a una pista que no existe. */
    private static Cursor hiddenCursor() {
        return new Cursor(-1, 0, 0, 1);
    }
}
