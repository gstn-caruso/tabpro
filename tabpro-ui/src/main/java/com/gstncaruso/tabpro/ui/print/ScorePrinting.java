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

    /**
     * El boton Configure del manual, en la ventana de Imprimir: deja elegir el papel y la
     * orientacion de la impresora misma. Es otro formato distinto del {@link PageSetup} de la
     * partitura -ese lo pide "Configurar pagina [F8]" y describe el documento, no el aparato.
     */
    public static void configurePrinterPage() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.pageDialog(job.defaultPage());
    }

    public static void exportImage(Score score, PageSetup setup, Path path, ViewMode viewMode, Zoom zoom) {
        String format = formatOf(path);
        if (format.equals("bmp") && viewMode != ViewMode.PAGE) {
            throw new ImageExportException("La exportación a BMP sólo está disponible en modo Página.");
        }
        writeImage(ScoreSheets.render(score, viewMode, zoom, setup), format, path);
    }

    /**
     * Escribe la imagen con ImageIO y no se conforma con que el metodo vuelva sin tirar
     * excepcion: {@code ImageIO.write} devuelve {@code false} (sin escribir nada) cuando ningun
     * escritor instalado puede codificar esa imagen en ese formato, y eso no puede pasar
     * desapercibido.
     */
    static void writeImage(BufferedImage image, String format, Path path) {
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
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "jpg";
        }
        return name.endsWith(".bmp") ? "bmp" : "png";
    }

    /**
     * Cada hoja de la partitura, una por hoja de papel de la impresora.
     *
     * <p>Visible para el paquete -y no privada- a proposito: es el {@link Printable} que la
     * impresora de verdad invoca, y es lo unico de esta clase que un test sin impresora necesita
     * construir a mano para ejercitarlo directamente con un {@link Graphics2D} y un
     * {@link PageFormat} armados en el test.
     */
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
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".bmp")
                ? file.toPath()
                : file.toPath().resolveSibling(name + ".png");
    }
}
