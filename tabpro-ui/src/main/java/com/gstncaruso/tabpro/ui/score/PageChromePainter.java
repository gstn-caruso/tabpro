package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.ui.page.BannerText;
import com.gstncaruso.tabpro.ui.page.PageElement;
import com.gstncaruso.tabpro.ui.page.PageMetrics;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * La hoja clara del Modo Pagina y del Modo Pergamino: sombra suave, margenes, y el encabezado y el
 * pie que pidio Configurar pagina. Que dice cada linea ya viene resuelto de la
 * {@link com.gstncaruso.tabpro.ui.page.PageBanner}; aca solo se decide con que letra y de que lado
 * se dibuja cada elemento.
 */
final class PageChromePainter {

    private static final Font TITLE_FONT = new Font(Font.SERIF, Font.BOLD, 20);
    private static final Font SUBTITLE_FONT = new Font(Font.SERIF, Font.PLAIN, 13);
    private static final Font CREDIT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    private static final Font FOOTER_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 9);

    /** Cuanto sube el pie sobre el borde del margen de abajo. */
    private static final int FOOTER_BASELINE_OVER_THE_MARGIN = 8;

    private PageChromePainter() {
    }

    static void paintSheet(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(ScoreColors.PAGE_SHADOW);
        g.fill(new RoundRectangle2D.Double(x + 5, y + 7, width, height, 6, 6));
        g.setColor(ScoreColors.PAGE_PAPER);
        g.fillRect(x, y, width, height);
    }

    /**
     * El encabezado entero va solo en la primera hoja; en las siguientes se repite apenas el
     * titulo, chiquito, como hace Guitar Pro.
     */
    static void paintHeader(Graphics2D g, List<BannerText> header, PageMetrics sheet, int y, boolean firstPage) {
        int centerX = sheet.pageWidth() / 2;
        int top = y + sheet.marginTop();
        if (!firstPage) {
            paintRunningHeader(g, header, centerX, top);
            return;
        }

        int centeredY = top + 20;
        int rightY = top + 20;
        for (BannerText line : header) {
            g.setFont(fontOf(line.element()));
            g.setColor(line.element() == PageElement.TITLE ? ScoreColors.PAGE_INK : ScoreColors.PAGE_MUTED);
            if (isRightAligned(line.element())) {
                drawRightAligned(g, line.text(), sheet.pageWidth() - sheet.marginRight(), rightY);
                rightY += lineHeightOf(g);
            } else {
                drawCentered(g, line.text(), centerX, centeredY);
                centeredY += lineHeightOf(g);
            }
        }
    }

    static void paintFooter(Graphics2D g, List<BannerText> footer, PageMetrics sheet, int y, int height) {
        g.setFont(FOOTER_FONT);
        g.setColor(ScoreColors.PAGE_MUTED);
        int baseline = y + height - sheet.marginBottom() - FOOTER_BASELINE_OVER_THE_MARGIN;
        int leftY = baseline;
        int rightY = baseline;
        for (BannerText line : footer) {
            if (isRightAligned(line.element())) {
                drawRightAligned(g, line.text(), sheet.pageWidth() - sheet.marginRight(), rightY);
                rightY -= lineHeightOf(g);
            } else {
                g.drawString(line.text(), sheet.marginLeft(), leftY);
                leftY -= lineHeightOf(g);
            }
        }
    }

    private static void paintRunningHeader(Graphics2D g, List<BannerText> header, int centerX, int top) {
        g.setColor(ScoreColors.PAGE_MUTED);
        g.setFont(CREDIT_FONT);
        header.stream()
                .filter(line -> line.element() == PageElement.TITLE)
                .findFirst()
                .ifPresent(title -> drawCentered(g, title.text(), centerX, top + 10));
    }

    private static Font fontOf(PageElement element) {
        return switch (element) {
            case TITLE -> TITLE_FONT;
            case SUBTITLE -> SUBTITLE_FONT;
            case COPYRIGHT, PAGE_NUMBER -> FOOTER_FONT;
            default -> CREDIT_FONT;
        };
    }

    /** Los creditos y el numero de pagina van contra el margen derecho; el resto, centrado. */
    private static boolean isRightAligned(PageElement element) {
        return element == PageElement.WORDS || element == PageElement.MUSIC || element == PageElement.PAGE_NUMBER;
    }

    private static int lineHeightOf(Graphics2D g) {
        return g.getFontMetrics().getHeight();
    }

    private static void drawCentered(Graphics2D g, String text, int centerX, int y) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, y);
    }

    private static void drawRightAligned(Graphics2D g, String text, int rightX, int y) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, rightX - metrics.stringWidth(text), y);
    }
}
