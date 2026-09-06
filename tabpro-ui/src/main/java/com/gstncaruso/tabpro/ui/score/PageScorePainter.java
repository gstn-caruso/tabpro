package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.editing.Cursor;
import com.gstncaruso.tabpro.core.editing.Selection;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.playback.Playhead;
import com.gstncaruso.tabpro.ui.page.PageMetrics;
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
 * sistemas en hojas claras del papel que pide la configuracion de pagina (via {@link PageLayout})
 * dibujando la tinta con {@link PaperRenderer}. Pantalla vertical y horizontal dibujan directo
 * sobre el fondo oscuro, sin hoja.
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
            List<PagePlacement> pages = placementsFor(layout, viewport);
            int height = pages.isEmpty() ? 0 : pages.get(pages.size() - 1).bottom();
            return scaled(viewport.sheet().pageWidth(), height, factor);
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

        for (PagePlacement page : placementsFor(layout, viewport)) {
            paintSheet(g, score, layout, cursor, playhead, selection, viewport, page, page.screenTop());
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
        PageMetrics sheet = viewport.sheet();
        for (PagePlacement page : placementsFor(layout, viewport)) {
            int contentTop = page.screenTop() + sheet.contentTop();
            if (y < contentTop || y >= contentTop + page.paintedHeight()) {
                continue;
            }
            return layout.hitTest(
                    unscaled(x - sheet.contentLeft(), sheet),
                    unscaled(y - contentTop, sheet) + page.shiftUp());
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
            PageMetrics sheet = viewport.sheet();
            for (PagePlacement page : placementsFor(layout, viewport)) {
                if (layout.systemOf(measure) < page.firstSystem() || layout.systemOf(measure) > page.lastSystem()) {
                    continue;
                }
                block = new Rectangle(
                        sheet.contentLeft() + scaled(block.x, sheet),
                        page.screenTop() + sheet.contentTop() + scaled(block.y - page.shiftUp(), sheet),
                        scaled(block.width, sheet),
                        scaled(block.height, sheet));
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
                ? viewport.sheet().layoutWidth()
                : (mode.scrollsHorizontally() ? UNWRAPPED_WIDTH : Math.max(200, viewport.width()));
        return ScoreLayout.of(score, width, viewport.visibleTracks(), viewport.visibleNotations());
    }

    private static void paintSheet(
            Graphics2D g, Score score, ScoreLayout layout, Cursor cursor, Playhead playhead,
            Optional<Selection> selection, ScoreViewport viewport, PagePlacement page, int top) {
        PageMetrics sheet = viewport.sheet();
        PageChromePainter.paintSheet(g, 0, top, sheet.pageWidth(), page.pageHeight());
        PageChromePainter.paintHeader(g, score.info(), sheet, top, page.isFirst());
        PageChromePainter.paintFooter(g, score.info(), sheet, top, page.pageHeight());

        BufferedImage ink = PaperRenderer.renderAsInk(
                sheet.contentWidth(), page.paintedHeight(), inkGraphics -> {
                    inkGraphics.scale(sheet.scoreScale(), sheet.scoreScale());
                    inkGraphics.translate(0, -page.shiftUp());
                    ScorePainter.paint(
                            inkGraphics, layout, score, cursor, playhead, selection,
                            viewport.highlighted(cursor.voice()), false);
                });
        g.drawImage(ink, sheet.contentLeft(), top + sheet.contentTop(), null);
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

    private static int scaled(int layoutUnits, PageMetrics sheet) {
        return (int) Math.round(layoutUnits * sheet.scoreScale());
    }

    private static int unscaled(int pixels, PageMetrics sheet) {
        return (int) Math.round(pixels / sheet.scoreScale());
    }

    private static List<PagePlacement> placementsFor(ScoreLayout layout, ScoreViewport viewport) {
        PageMetrics sheet = viewport.sheet();
        PageLayout pages = viewport.mode().paginates()
                ? PageLayout.paginated(layout, sheet.layoutHeight())
                : PageLayout.parchment(layout);
        List<PagePlacement> placements = new ArrayList<>();
        int screenTop = 0;
        for (int page = 0; page < pages.pageCount(); page++) {
            int painted = Math.max(1, scaled(pages.contentHeightOf(page), sheet));
            int pageHeight = sheet.pageHeight();
            if (viewport.mode().paginates()) {
                painted = Math.min(painted, sheet.contentHeight());
            } else {
                pageHeight = sheet.contentTop() + painted + PageMetrics.FOOTER_HEIGHT + sheet.marginBottom();
            }
            placements.add(new PagePlacement(
                    screenTop, pageHeight, painted, layout.systemTop(pages.firstSystemOf(page)),
                    pages.firstSystemOf(page), pages.lastSystemOf(page), page == 0));
            screenTop += pageHeight + PageMetrics.PAGE_GAP;
        }
        return placements;
    }

    /**
     * Una hoja puesta en su lugar: donde arranca en pantalla, cuanto mide el papel, cuanto ocupa
     * su contenido ya dibujado -o sea con el tamano de la partitura aplicado- y cuanto hay que
     * subir la partitura para que el primer sistema de la hoja quede arriba de todo.
     */
    private record PagePlacement(
            int screenTop, int pageHeight, int paintedHeight, int shiftUp,
            int firstSystem, int lastSystem, boolean isFirst) {

        int bottom() {
            return screenTop + pageHeight;
        }
    }
}
