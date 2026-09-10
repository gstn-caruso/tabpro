package com.gstncaruso.tabpro.ui.print;

import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.page.PageMetrics;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.score.ViewMode;
import com.gstncaruso.tabpro.ui.score.Zoom;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public final class ScorePrinting {

    private final Printing printing;
    private PageFormat pageFormat;

    public ScorePrinting(Printing printing) {
        this.printing = printing;
    }

    public void print(Score score, PageSetup setup, PrintSettings settings, String jobName)
            throws PrinterException {
        printing.setJobName(jobName);
        printing.setPrintable(new ScorePages(score, setup, settings), currentPageFormat());
        if (printing.printDialog()) {
            printing.print();
        }
    }

    public static int pageCount(Score score, PageSetup setup) {
        return ScoreSheets.pageCount(score, setup);
    }

    public void configurePrinterPage() {
        pageFormat = printing.pageDialog(currentPageFormat());
    }

    private PageFormat currentPageFormat() {
        if (pageFormat == null) {
            pageFormat = printing.defaultPage();
        }
        return pageFormat;
    }

    public static void exportImage(Score score, PageSetup setup, Path path, ViewMode viewMode, Zoom zoom) {
        String format = formatOf(path);
        if (format.equals("bmp") && viewMode != ViewMode.PAGE) {
            throw new ImageExportException("La exportación a BMP sólo está disponible en modo Página.");
        }
        writeImage(ScoreSheets.render(score, viewMode, zoom, setup), format, path);
    }

    /**
     * {@code ImageIO.write} returns {@code false} without writing anything and without throwing
     * when no installed writer can encode the image in that format.
     */
    static void writeImage(BufferedImage image, String format, Path path) {
        if (format.equals("bmp") && BmpDocument.canEncode(image)) {
            try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(path)) {
                BmpDocument.writeTo(image, out);
            } catch (IOException e) {
                throw new UncheckedIOException("no se pudo escribir " + path, e);
            }
            return;
        }
        boolean escrita;
        try {
            escrita = ImageIO.write(image, format, path.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException("no se pudo escribir " + path, e);
        }
        if (!escrita) {
            throw new ImageExportException(
                    "No se pudo exportar la imagen en formato " + format.toUpperCase(java.util.Locale.ROOT)
                            + ": ningún códec de imagen instalado sabe codificarla en ese formato.");
        }
    }

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
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "jpg";
        }
        return name.endsWith(".bmp") ? "bmp" : "png";
    }

    record ScorePages(Score score, PageSetup setup, PrintSettings settings) implements Printable {

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
            canvas.translate(horizontalCenteringOffset(format, sheet, scale), 0);
            canvas.scale(scale, scale);
            ScoreSheets.paintPageOn(canvas, score, Zoom.whole(), setup, settings.sheetAt(pageIndex) - 1);
            canvas.dispose();
            return PAGE_EXISTS;
        }

        private double horizontalCenteringOffset(PageFormat format, Dimension sheet, double scale) {
            if (!settings.centeredDocument()) {
                return 0;
            }
            return Math.max(0, (format.getImageableWidth() - sheet.width * scale) / 2);
        }
    }

    public static Path withImageExtension(File file) {
        String name = file.getName();
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".bmp")
                ? file.toPath()
                : file.toPath().resolveSibling(name + ".png");
    }
}
