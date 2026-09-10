package com.gstncaruso.tabpro.ui.print;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.score.ViewMode;
import com.gstncaruso.tabpro.ui.score.Zoom;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScorePrintingTest {

    private static final PageSetup A4 = PageSetup.defaults();

    @Test
    void alAceptarElDialogoLlegaElTrabajoYElPrintableQueDibujaLaPartituraReal() throws PrinterException {
        Score score = scoreWithMeasures(4);
        PrintSettings settings = PrintSettings.everything(ScoreSheets.pageCount(score, A4));
        RecordingPrinting printing = new RecordingPrinting();
        ScorePrinting scorePrinting = new ScorePrinting(printing);

        scorePrinting.print(score, A4, settings, "mi-partitura.tab");

        assertEquals("mi-partitura.tab", printing.jobName());
        assertTrue(printing.printCalled(), "si el dialogo se acepta, tiene que llegar a imprimir de verdad");
        assertEquals(
                new ScorePrinting.ScorePages(score, A4, settings), printing.printable(),
                "el Printable recibido tiene que ser el que pinta esta partitura real (ver ScorePagesTest)");
    }

    @Test
    void siElDialogoDeImprimirSeCancelaNuncaLlegaAImprimirDeVerdad() throws PrinterException {
        Score score = scoreWithMeasures(4);
        RecordingPrinting printing = new RecordingPrinting();
        printing.cancelPrintDialog();
        ScorePrinting scorePrinting = new ScorePrinting(printing);

        scorePrinting.print(score, A4, PrintSettings.everything(1), "mi-partitura.tab");

        assertFalse(printing.printCalled(), "si se cancela el dialogo, no tiene que llegar a imprimir de verdad");
    }

    @Test
    void elPageFormatElegidoAlConfigurarSeConservaParaLaProximaImpresion() throws PrinterException {
        Score score = scoreWithMeasures(4);
        RecordingPrinting printing = new RecordingPrinting();
        PageFormat elegido = new PageFormat();
        printing.chooseInPageDialog(elegido);
        ScorePrinting scorePrinting = new ScorePrinting(printing);

        scorePrinting.configurePrinterPage();
        scorePrinting.print(score, A4, PrintSettings.everything(1), "mi-partitura.tab");

        assertSame(elegido, printing.printableFormat(),
                "el PageFormat elegido en Configurar tiene que ser el que se usa en la proxima impresion");
    }

    @Tag("integracion")
    @Test
    void exportaUnBmpDeVerdadEnModoPagina(@TempDir Path tempDir) throws IOException {
        Score score = scoreWithMeasures(4);
        Path path = tempDir.resolve("partitura.bmp");

        ScorePrinting.exportImage(score, A4, path, ViewMode.PAGE, Zoom.whole());

        assertTrue(Files.exists(path));
        BufferedImage esperada = ScoreSheets.render(score, Zoom.whole(), A4);
        BufferedImage leida = ImageIO.read(path.toFile());

        assertEquals(esperada.getWidth(), leida.getWidth());
        assertEquals(esperada.getHeight(), leida.getHeight());
        assertEquals(pixelsOf(esperada), pixelsOf(leida), "el bmp tiene que verse igual que el render en memoria");
        // Guarda especifica contra la trampa del canal alfa: si el bmp saliera todo blanco o
        // todo negro de un solo color (por escribir un TYPE_INT_ARGB tal cual, que ImageIO ni
        // siquiera logra escribir), esto lo detecta sin acoplarse a los colores exactos del tema.
        assertTrue(distinctColorsOf(leida).size() > 1, "la imagen no puede salir de un solo color");
    }

    @Tag("integracion")
    @Test
    void elBmpExportadoEsIdenticoAlQueEscribeBmpDocument(@TempDir Path tempDir) throws IOException {
        Score score = scoreWithMeasures(4);
        Path path = tempDir.resolve("partitura.bmp");

        ScorePrinting.exportImage(score, A4, path, ViewMode.PAGE, Zoom.whole());

        java.io.ByteArrayOutputStream esperado = new java.io.ByteArrayOutputStream();
        BmpDocument.writeTo(ScoreSheets.render(score, Zoom.whole(), A4), esperado);
        assertArrayEquals(esperado.toByteArray(), Files.readAllBytes(path),
                "el bmp exportado tiene que ser el que escribe BmpDocument, no otro codec");
    }

    @Test
    void bmpFueraDelModoPaginaAvisaYNoEscribeNada(@TempDir Path tempDir) {
        Score score = scoreWithMeasures(4);
        Path path = tempDir.resolve("partitura.bmp");

        ImageExportException error = assertThrows(ImageExportException.class,
                () -> ScorePrinting.exportImage(score, A4, path, ViewMode.SCREEN_VERTICAL, Zoom.whole()));

        assertTrue(error.getMessage().toLowerCase(java.util.Locale.ROOT).contains("bmp"));
        assertFalse(Files.exists(path), "no tiene que quedar un archivo a medio escribir");
    }

    @Tag("integracion")
    @Test
    void pngFueraDelModoPaginaSeExportaSinProblema(@TempDir Path tempDir) {
        Score score = scoreWithMeasures(4);
        Path path = tempDir.resolve("partitura.png");

        ScorePrinting.exportImage(score, A4, path, ViewMode.SCREEN_VERTICAL, Zoom.whole());

        assertTrue(Files.exists(path), "la restriccion es solo para bmp");
    }

    @Tag("integracion")
    @Test
    void exportaLaImagenConElZoomQueTieneLaVentana(@TempDir Path tempDir) throws IOException {
        Score score = scoreWithMeasures(4);
        Path al100 = tempDir.resolve("al-100.png");
        Path al200 = tempDir.resolve("al-200.png");

        ScorePrinting.exportImage(score, A4, al100, ViewMode.PAGE, new Zoom(100));
        ScorePrinting.exportImage(score, A4, al200, ViewMode.PAGE, new Zoom(200));

        BufferedImage imagenAl100 = ImageIO.read(al100.toFile());
        BufferedImage imagenAl200 = ImageIO.read(al200.toFile());

        assertNotEquals(imagenAl100.getWidth(), imagenAl200.getWidth(),
                "la misma partitura al 100% y al 200% no puede dar el mismo ancho en pixeles");
        assertNotEquals(imagenAl100.getHeight(), imagenAl200.getHeight(),
                "la misma partitura al 100% y al 200% no puede dar el mismo alto en pixeles");
    }

    @Tag("integracion")
    @Test
    void exportaLaImagenConElModoPergaminoSinSaltosDePagina(@TempDir Path tempDir) throws IOException {
        Score score = scoreWithMeasures(16);
        Path enPagina = tempDir.resolve("pagina.png");
        Path enPergamino = tempDir.resolve("pergamino.png");

        ScorePrinting.exportImage(score, A4, enPagina, ViewMode.PAGE, Zoom.whole());
        ScorePrinting.exportImage(score, A4, enPergamino, ViewMode.PARCHMENT, Zoom.whole());

        BufferedImage imagenEnPagina = ImageIO.read(enPagina.toFile());
        BufferedImage imagenEnPergamino = ImageIO.read(enPergamino.toFile());

        assertNotEquals(imagenEnPagina.getHeight(), imagenEnPergamino.getHeight(),
                "en pergamino no hay saltos de pagina: el alto tiene que ser otro que en modo Pagina");
    }

    @Test
    void unaImagenQueImageIoNoPuedeCodificarEnBmpAvisaEnVezDeQuedarseCallada(@TempDir Path tempDir) {
        Path path = tempDir.resolve("no-se-puede.bmp");
        BufferedImage imagenConAlfaReal = imagenConTransparenciaReal();

        ImageExportException error = assertThrows(ImageExportException.class,
                () -> ScorePrinting.writeImage(imagenConAlfaReal, "bmp", path));

        assertTrue(error.getMessage().toLowerCase(java.util.Locale.ROOT).contains("bmp"),
                "el mensaje tiene que decir en que formato fallo");
        assertFalse(Files.exists(path), "si ImageIO no pudo escribir nada, no puede quedar un archivo");
    }

    @Test
    void unaImagenQueImageIoNoPuedeCodificarEnJpgAvisaEnVezDeQuedarseCallada(@TempDir Path tempDir) {
        Path path = tempDir.resolve("no-se-puede.jpg");
        BufferedImage imagenConAlfaReal = imagenConTransparenciaReal();

        ImageExportException error = assertThrows(ImageExportException.class,
                () -> ScorePrinting.writeImage(imagenConAlfaReal, "jpg", path));

        assertTrue(error.getMessage().toLowerCase(java.util.Locale.ROOT).contains("jpg"),
                "el mensaje tiene que decir en que formato fallo");
        assertFalse(Files.exists(path), "si ImageIO no pudo escribir nada, no puede quedar un archivo");
    }

    /**
     * TYPE_INT_ARGB con un pixel realmente translucido: ImageIO.write devuelve false para BMP y
     * JPG porque ninguno de los dos soporta canal alfa, y no escribe nada. No hace falta pasar por
     * el render de la partitura para reproducir el modo de falla silencioso.
     */
    private static BufferedImage imagenConTransparenciaReal() {
        BufferedImage imagen = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        imagen.setRGB(0, 0, 0x80FF0000);
        return imagen;
    }

    private static java.util.Set<Integer> distinctColorsOf(BufferedImage image) {
        java.util.Set<Integer> colors = new java.util.HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                colors.add(image.getRGB(x, y));
            }
        }
        return colors;
    }

    private static String pixelsOf(BufferedImage image) {
        StringBuilder pixels = new StringBuilder();
        for (int y = 0; y < image.getHeight(); y += 7) {
            for (int x = 0; x < image.getWidth(); x += 7) {
                pixels.append(image.getRGB(x, y)).append(' ');
            }
        }
        return pixels.toString();
    }

    private static Score scoreWithMeasures(int count) {
        List<Measure> measures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            measures.add(new Measure(TimeSignature.fourFour(), List.of(
                    Beat.of(Duration.quarter(), new Note(1, i % 5)),
                    Beat.of(Duration.quarter(), new Note(1, i % 5)),
                    Beat.of(Duration.quarter(), new Note(1, i % 5)),
                    Beat.of(Duration.quarter(), new Note(1, i % 5)))));
        }
        Track guitar = Track.standardGuitar("Guitarra");
        return new Score("", 120, List.of(new Track("Guitarra", guitar.tuning(), guitar.channel(), measures)));
    }
}
