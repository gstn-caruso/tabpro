package com.gstncaruso.tabpro.ui.dialogs.ascii;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;

/** Imprime el texto de una tablatura ASCII, como piden los botones Imprimir del manual. */
final class AsciiPrinting {

    private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 10);

    private AsciiPrinting() {
    }

    static void print(String text, String jobName) throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName(jobName);
        job.setPrintable(new TextPages(text.lines().toList()));
        if (job.printDialog()) {
            job.print();
        }
    }

    private record TextPages(List<String> lines) implements Printable {

        @Override
        public int print(Graphics graphics, PageFormat format, int pageIndex) {
            Graphics2D canvas = (Graphics2D) graphics;
            canvas.setFont(FONT);
            FontMetrics metrics = canvas.getFontMetrics();
            int lineHeight = metrics.getHeight();
            int linesPerPage = Math.max(1, (int) (format.getImageableHeight() / lineHeight));
            int firstLine = pageIndex * linesPerPage;
            if (firstLine >= lines.size()) {
                return NO_SUCH_PAGE;
            }
            canvas.translate(format.getImageableX(), format.getImageableY());
            int y = metrics.getAscent();
            for (int i = firstLine; i < Math.min(lines.size(), firstLine + linesPerPage); i++) {
                canvas.drawString(lines.get(i), 0, y);
                y += lineHeight;
            }
            return PAGE_EXISTS;
        }
    }
}
