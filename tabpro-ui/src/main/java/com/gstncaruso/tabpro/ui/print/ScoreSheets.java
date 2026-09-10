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

public final class ScoreSheets {

    /**
     * The sheet is drawn without an alpha channel on purpose: BMP export cannot write
     * transparency and fails silently, {@code ImageIO.write} returns false, throws no
     * exception and leaves no file behind.
     */
    private static final int OPAQUE_PAPER = BufferedImage.TYPE_INT_RGB;

    private ScoreSheets() {
    }

    public static Dimension pageSize(Zoom zoom, PageSetup setup) {
        PageMetrics sheet = PageMetrics.of(setup);
        return new Dimension(
                (int) Math.round(sheet.pageWidth() * zoom.factor()),
                (int) Math.round(sheet.pageHeight() * zoom.factor()));
    }

    public static int pageCount(Score score, PageSetup setup) {
        return PageScorePainter.pageCount(score, sheetViewport(Zoom.whole(), setup));
    }

    public static BufferedImage render(Score score, ViewMode viewMode, Zoom zoom, PageSetup setup) {
        ScoreViewport viewport = viewportFor(viewMode, zoom, setup);
        Dimension size = PageScorePainter.canvasSize(score, viewport);
        return drawnOn(size, graphics -> paintOn(graphics, score, viewport));
    }

    public static BufferedImage render(Score score, Zoom zoom, PageSetup setup) {
        return render(score, ViewMode.PAGE, zoom, setup);
    }

    public static BufferedImage renderPage(Score score, Zoom zoom, PageSetup setup, int page) {
        return drawnOn(pageSize(zoom, setup), graphics -> paintPageOn(graphics, score, zoom, setup, page));
    }

    public static List<BufferedImage> renderPages(Score score, Zoom zoom, PageSetup setup) {
        List<BufferedImage> sheets = new ArrayList<>();
        for (int page = 0; page < pageCount(score, setup); page++) {
            sheets.add(renderPage(score, zoom, setup, page));
        }
        return List.copyOf(sheets);
    }

    public static void paintOn(Graphics2D graphics, Score score, ViewMode viewMode, Zoom zoom, PageSetup setup) {
        paintOn(graphics, score, viewportFor(viewMode, zoom, setup));
    }

    public static void paintOn(Graphics2D graphics, Score score, Zoom zoom, PageSetup setup) {
        paintOn(graphics, score, ViewMode.PAGE, zoom, setup);
    }

    private static void paintOn(Graphics2D graphics, Score score, ScoreViewport viewport) {
        PageScorePainter.paint(
                graphics, score, hiddenCursor(), Playhead.silent(), Optional.empty(), viewport);
    }

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

    private static ScoreViewport sheetViewport(Zoom zoom, PageSetup setup) {
        return viewportFor(ViewMode.PAGE, zoom, setup);
    }

    private static ScoreViewport viewportFor(ViewMode viewMode, Zoom zoom, PageSetup setup) {
        return ScoreViewport.of(viewMode, zoom, PageMetrics.of(setup).pageWidth()).withPageSetup(setup);
    }

    private static Cursor hiddenCursor() {
        return new Cursor(-1, 0, 0, 1);
    }
}
