package com.gstncaruso.tabpro.ui.print;

import com.gstncaruso.tabpro.core.model.Score;
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

    /** Abre el diálogo de impresión del sistema y manda la partitura. */
    public static void print(Score score, String jobName) throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName(jobName);
        job.setPrintable(new ScorePages(score));
        if (job.printDialog()) {
            job.print();
        }
    }

    public static void exportImage(Score score, Path path) {
        try {
            ImageIO.write(ScoreSheets.render(score, Zoom.whole()), formatOf(path), path.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException("no se pudo escribir " + path, e);
        }
    }

    /** Exporta la partitura a PDF, una hoja por pagina. */
    public static void exportPdf(Score score, Path path) {
        PdfDocument pdf = new PdfDocument();
        ScoreSheets.renderPages(score, Zoom.whole()).forEach(pdf::addPage);
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

    /** Reparte la partitura en las hojas que le entren al papel de la impresora. */
    private record ScorePages(Score score) implements Printable {

        @Override
        public int print(Graphics graphics, PageFormat format, int pageIndex) {
            Dimension sheet = ScoreSheets.sizeOf(score, Zoom.whole());
            double scale = format.getImageableWidth() / sheet.width;
            int pageHeight = (int) Math.max(1, format.getImageableHeight() / scale);
            int pages = (int) Math.ceil(sheet.height / (double) pageHeight);
            if (pageIndex >= pages) {
                return NO_SUCH_PAGE;
            }
            Graphics2D canvas = (Graphics2D) graphics.create();
            canvas.translate(format.getImageableX(), format.getImageableY());
            canvas.scale(scale, scale);
            canvas.translate(0, -pageIndex * (double) pageHeight);
            canvas.clipRect(0, pageIndex * pageHeight, sheet.width, pageHeight);
            ScoreSheets.paintOn(canvas, score, Zoom.whole());
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
