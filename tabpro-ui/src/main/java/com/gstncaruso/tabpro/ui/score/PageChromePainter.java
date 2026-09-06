package com.gstncaruso.tabpro.ui.score;

import com.gstncaruso.tabpro.core.model.ScoreInfo;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/** La hoja clara del Modo Pagina y del Modo Pergamino: sombra suave, margenes, encabezado con
 * titulo/subtitulo/artista/album/creditos y pie con copyright y transcriptor. */
final class PageChromePainter {

    private static final Font TITLE_FONT = new Font(Font.SERIF, Font.BOLD, 20);
    private static final Font SUBTITLE_FONT = new Font(Font.SERIF, Font.PLAIN, 13);
    private static final Font CREDIT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    private static final Font FOOTER_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 9);

    private PageChromePainter() {
    }

    static void paintSheet(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(ScoreColors.PAGE_SHADOW);
        g.fill(new RoundRectangle2D.Double(x + 5, y + 7, width, height, 6, 6));
        g.setColor(ScoreColors.PAGE_PAPER);
        g.fillRect(x, y, width, height);
    }

    static void paintHeader(Graphics2D g, ScoreInfo info, int x, int y, int width, int margin, boolean firstPage) {
        int centerX = x + width / 2;
        int top = y + margin;

        if (!firstPage) {
            paintRunningHeader(g, info, x, top, width);
            return;
        }

        g.setColor(ScoreColors.PAGE_INK);
        g.setFont(TITLE_FONT);
        drawCentered(g, info.heading().isBlank() ? "Sin titulo" : titleOnly(info), centerX, top + 20);

        if (!info.subtitle().isBlank()) {
            g.setFont(SUBTITLE_FONT);
            drawCentered(g, info.subtitle(), centerX, top + 38);
        }

        g.setFont(CREDIT_FONT);
        g.setColor(ScoreColors.PAGE_MUTED);
        if (!info.artist().isBlank()) {
            drawCentered(g, info.artist(), centerX, top + 54);
        }
        if (!info.album().isBlank()) {
            drawCentered(g, info.album(), centerX, top + 66);
        }

        String credits = info.credits();
        if (!credits.isBlank()) {
            int lineY = top + 20;
            for (String line : credits.split("\n")) {
                drawRightAligned(g, line, x + width - margin, lineY);
                lineY += 12;
            }
        }
    }

    private static void paintRunningHeader(Graphics2D g, ScoreInfo info, int x, int top, int width) {
        g.setColor(ScoreColors.PAGE_MUTED);
        g.setFont(CREDIT_FONT);
        drawCentered(g, titleOnly(info), x + width / 2, top + 10);
    }

    private static String titleOnly(ScoreInfo info) {
        return info.title().isBlank() ? info.heading() : info.title();
    }

    static void paintFooter(Graphics2D g, ScoreInfo info, int x, int y, int width, int height, int margin) {
        int baseline = y + height - margin + 4;
        g.setFont(FOOTER_FONT);
        g.setColor(ScoreColors.PAGE_MUTED);
        if (!info.copyright().isBlank()) {
            g.drawString(info.copyright(), x + margin, baseline);
        }
        if (!info.transcriber().isBlank()) {
            drawRightAligned(g, "Transcripcion: " + info.transcriber(), x + width - margin, baseline);
        }
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
