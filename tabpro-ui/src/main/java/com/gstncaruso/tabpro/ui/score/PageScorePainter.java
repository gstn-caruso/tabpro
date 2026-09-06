package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Selection;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.Playhead;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * El punto de entrada unico de la partitura para la ventana principal: elige como envolver los
 * compases segun el {@link ViewMode}, aplica el {@link Zoom}, y en Pagina/Pergamino reparte los
 * sistemas en hojas claras (via {@link PageLayout}) dibujando la tinta con {@link PaperRenderer}.
 * Pantalla vertical y horizontal dibujan directo sobre el fondo oscuro, sin hoja.
 */
public final class PageScorePainter {

    /** Ancho de columna que usa el Modo Pantalla Horizontal para no envolver nunca en sistemas. */
    private static final int UNWRAPPED_WIDTH = 1_000_000;

    private PageScorePainter() {
    }

    public static Dimension canvasSize(Score score, ScoreViewport viewport) {
        ViewMode mode = viewport.mode();
        ScoreLayout layout = layoutFor(score, viewport);
        double factor = viewport.factor();
        if (mode.showsPaper()) {
            List<PagePlacement> pages = placementsFor(layout, mode);
            int height = pages.isEmpty() ? 0 : pages.get(pages.size() - 1).bottom();
            return scaled(PageLayout.PAGE_WIDTH, height, factor);
        }
        int width = mode.scrollsHorizontally() ? naturalWidthOf(layout) : viewport.width();
        return scaled(width, layout.totalHeight(), factor);
    }

    public static void paint(
            Graphics2D g, Score score, Cursor cursor, Playhead playhead, Optional<Selection> selection,
            ScoreViewport viewport) {
        ViewMode mode = viewport.mode();
        ScoreLayout layout = layoutFor(score, viewport);
        g.scale(viewport.factor(), viewport.factor());

        if (!mode.showsPaper()) {
            ScorePainter.paint(
                    g, layout, score, cursor, playhead, selection, viewport.highlighted(cursor.voice()), true);
            return;
        }

        for (PagePlacement page : placementsFor(layout, mode)) {
            PageChromePainter.paintSheet(g, 0, page.screenTop(), PageLayout.PAGE_WIDTH, page.pageHeight());
            PageChromePainter.paintHeader(
                    g, score.info(), 0, page.screenTop(), PageLayout.PAGE_WIDTH, PageLayout.PAGE_MARGIN, page.isFirst());
            PageChromePainter.paintFooter(
                    g, score.info(), 0, page.screenTop(), PageLayout.PAGE_WIDTH, page.pageHeight(), PageLayout.PAGE_MARGIN);

            BufferedImage ink = PaperRenderer.renderAsInk(PageLayout.PAGE_WIDTH, page.contentHeight(), inkGraphics -> {
                inkGraphics.translate(0, -page.shiftUp());
                ScorePainter.paint(
                        inkGraphics, layout, score, cursor, playhead, selection,
                        viewport.highlighted(cursor.voice()), false);
            });
            g.drawImage(ink, 0, page.contentTop(), null);
        }
    }

    /** Traduce un clic de pantalla (ya sin el factor de zoom) a compas/beat/cuerda. */
    public static Optional<ScoreLayout.Hit> hitTest(
            Score score, ScoreViewport viewport, int screenX, int screenY) {
        ViewMode mode = viewport.mode();
        ScoreLayout layout = layoutFor(score, viewport);
        double factor = viewport.factor();
        int x = (int) Math.round(screenX / factor);
        int y = (int) Math.round(screenY / factor);
        if (!mode.showsPaper()) {
            return layout.hitTest(x, y);
        }
        for (PagePlacement page : placementsFor(layout, mode)) {
            if (y < page.contentTop() || y >= page.contentTop() + page.contentHeight()) {
                continue;
            }
            return layout.hitTest(x, y - page.contentTop() + page.shiftUp());
        }
        return Optional.empty();
    }

    /** Donde cae, en coordenadas de pantalla ya con el zoom aplicado, un beat de la partitura. */
    public static Rectangle boundsOf(
            Score score, ScoreViewport viewport, int track, int measure, int beat) {
        ViewMode mode = viewport.mode();
        ScoreLayout layout = layoutFor(score, viewport);
        Rectangle bounds = layout.beatBounds(track, measure, beat);
        int top = layout.trackTop(track, measure);
        Rectangle block = new Rectangle(bounds.x, top, bounds.width, layout.trackHeight(track));
        if (mode.showsPaper()) {
            for (PagePlacement page : placementsFor(layout, mode)) {
                if (layout.systemOf(measure) < page.firstSystem() || layout.systemOf(measure) > page.lastSystem()) {
                    continue;
                }
                block.y = block.y - page.shiftUp() + page.contentTop();
                break;
            }
        }
        double factor = viewport.factor();
        return new Rectangle(
                (int) Math.round(block.x * factor), (int) Math.round(block.y * factor),
                (int) Math.round(block.width * factor), (int) Math.round(block.height * factor));
    }

    static ScoreLayout layoutFor(Score score, ScoreViewport viewport) {
        ViewMode mode = viewport.mode();
        int width = mode.showsPaper()
                ? PageLayout.PAGE_WIDTH - 2 * PageLayout.PAGE_MARGIN
                : (mode.scrollsHorizontally() ? UNWRAPPED_WIDTH : Math.max(200, viewport.width()));
        return ScoreLayout.of(score, width, viewport.visibleTracks(), viewport.visibleNotations());
    }

    private static int naturalWidthOf(ScoreLayout layout) {
        if (layout.measureCount() == 0) {
            return ScoreLayout.LEFT_MARGIN + ScoreLayout.RIGHT_MARGIN;
        }
        int last = layout.measureCount() - 1;
        return layout.measureX(last) + layout.measureWidth(last) + ScoreLayout.RIGHT_MARGIN;
    }

    private static Dimension scaled(int width, int height, double factor) {
        return new Dimension((int) Math.round(width * factor), (int) Math.round(Math.max(height, 0) * factor));
    }

    private static List<PagePlacement> placementsFor(ScoreLayout layout, ViewMode mode) {
        PageLayout pages = PageLayout.of(layout, mode.paginates());
        List<PagePlacement> placements = new ArrayList<>();
        int screenTop = 0;
        for (int page = 0; page < pages.pageCount(); page++) {
            int contentHeight = pages.contentHeightOf(page);
            int pageHeight = mode.paginates()
                    ? PageLayout.PAGE_HEIGHT
                    : contentHeight + 2 * PageLayout.PAGE_MARGIN + PageLayout.HEADER_HEIGHT + PageLayout.FOOTER_HEIGHT;
            int contentTop = screenTop + PageLayout.PAGE_MARGIN + PageLayout.HEADER_HEIGHT;
            int shiftUp = layout.systemTop(pages.firstSystemOf(page));
            placements.add(new PagePlacement(
                    screenTop, pageHeight, contentTop, contentHeight, shiftUp,
                    pages.firstSystemOf(page), pages.lastSystemOf(page), page == 0));
            screenTop += pageHeight + PageLayout.PAGE_GAP;
        }
        return placements;
    }

    private record PagePlacement(
            int screenTop, int pageHeight, int contentTop, int contentHeight, int shiftUp,
            int firstSystem, int lastSystem, boolean isFirst) {

        int bottom() {
            return screenTop + pageHeight;
        }
    }
}
