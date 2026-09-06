package com.gstncaruso.tabpro.ui.print;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.page.PageMetrics;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.score.Zoom;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Imprimir la partitura y guardarla como imagen, como pide el manual. */
public final class ScorePrinting {

    private ScorePrinting() {
    }

    /**
     * Manda a la impresora las hojas que se pidieron, con el tamano que se pidio. Que hojas y de
     * que tamano ya lo decidio la ventana de Imprimir; el dialogo del sistema queda solo para
     * elegir la impresora y su papel.
     */
    public static void print(Score score, PageSetup setup, PrintSettings settings, String jobName)
            throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName(jobName);
        job.setPrintable(new ScorePages(score, setup, settings));
        if (job.printDialog()) {
            job.print();
        }
    }

    /** En cuantas hojas se reparte la partitura, que es lo que la ventana de Imprimir necesita saber. */
    public static int pageCount(Score score, PageSetup setup) {
        return ScoreSheets.pageCount(score, setup);
    }

    public static void exportImage(Score score, PageSetup setup, Path path) {
        try {
            ImageIO.write(ScoreSheets.render(score, Zoom.whole(), setup), formatOf(path), path.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException("no se pudo escribir " + path, e);
        }
    }

    /** Exporta la partitura a PDF, una hoja por pagina. */
    public static void exportPdf(Score score, PageSetup setup, Path path) {
        PageMetrics sheet = PageMetrics.of(setup);
        PdfDocument pdf = new PdfDocument(sheet.pageWidthPoints(), sheet.pageHeightPoints());
        ScoreSheets.renderPages(score, Zoom.whole(), setup).forEach(pdf::addPage);
        try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(path)) {
            pdf.writeTo(out);
        } catch (IOException e) {
            throw new UncheckedIOException("no se pudo escribir " + path, e);
        }
    }

    public static Path withPdfExtension(File file) {
        String name = file.getName();
        return name.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")
                ? file.toPath()
                : file.toPath().resolveSibling(name + ".pdf");
    }

    private static String formatOf(Path path) {
        String name = path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ? "jpg" : "png";
    }

    /** Cada hoja de la partitura, una por hoja de papel de la impresora. */
    private record ScorePages(Score score, PageSetup setup, PrintSettings settings) implements Printable {

        @Override
        public int print(Graphics graphics, PageFormat format, int pageIndex) {
            if (pageIndex >= settings.sheetsToPrint()) {
                return NO_SUCH_PAGE;
            }
            Dimension sheet = ScoreSheets.pageSize(Zoom.whole(), setup);
            double scale = settings.scaleFor(
                    sheet.width, sheet.height, format.getImageableWidth(), format.getImageableHeight());
            Graphics2D canvas = (Graphics2D) graphics.create();
            canvas.translate(format.getImageableX(), format.getImageableY());
            canvas.clipRect(0, 0, (int) format.getImageableWidth(), (int) format.getImageableHeight());
            canvas.scale(scale, scale);
            ScoreSheets.paintPageOn(canvas, score, Zoom.whole(), setup, settings.sheetAt(pageIndex) - 1);
            canvas.dispose();
            return PAGE_EXISTS;
        }
    }

    /** El archivo con la extension que le corresponde a la imagen. */
    public static Path withImageExtension(File file) {
        String name = file.getName();
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                ? file.toPath()
                : file.toPath().resolveSibling(name + ".png");
    }
}
