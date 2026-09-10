package com.gstncaruso.tabpro.ui.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Beat;
import com.gstncaruso.tabpro.core.model.Duration;
import com.gstncaruso.tabpro.core.model.Measure;
import com.gstncaruso.tabpro.core.model.Note;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.score.RecordingCanvas;
import com.gstncaruso.tabpro.ui.score.Zoom;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScorePagesTest {

    private static final PageSetup A4 = PageSetup.defaults();

    @Test
    void unaPartituraCortaTieneUnaSolaHojaYNoSuchPageMarcaElLimite() {
        Score corta = scoreWithMeasures(4);
        int total = ScoreSheets.pageCount(corta, A4);
        assertEquals(1, total, "esta partitura corta tiene que entrar en una sola hoja");

        ScorePrinting.ScorePages paginas = new ScorePrinting.ScorePages(corta, A4, PrintSettings.everything(total));
        PageFormat papel = pageFormatOf(ScoreSheets.pageSize(Zoom.whole(), A4));

        assertEquals(Printable.PAGE_EXISTS, imprimirEnLienzo(paginas, papel, 0));
        assertEquals(Printable.NO_SUCH_PAGE, imprimirEnLienzo(paginas, papel, 1));
    }

    @Test
    void unaPartituraLargaTieneVariasHojasYNoSuchPageMarcaElLimite() {
        Score larga = scoreWithMeasures(40);
        int total = ScoreSheets.pageCount(larga, A4);
        assertTrue(total > 1, "esta partitura larga tiene que necesitar mas de una hoja");

        ScorePrinting.ScorePages paginas = new ScorePrinting.ScorePages(larga, A4, PrintSettings.everything(total));
        PageFormat papel = pageFormatOf(ScoreSheets.pageSize(Zoom.whole(), A4));

        for (int i = 0; i < total; i++) {
            assertEquals(Printable.PAGE_EXISTS, imprimirEnLienzo(paginas, papel, i),
                    "la hoja " + i + " tiene que existir");
        }
        assertEquals(Printable.NO_SUCH_PAGE, imprimirEnLienzo(paginas, papel, total),
                "despues de la ultima hoja no puede haber una pagina mas");
    }

    @Test
    void cadaHojaDibujaAlgoDistintoALaAnterior() {
        Score larga = scoreWithMeasures(40);
        int total = ScoreSheets.pageCount(larga, A4);
        ScorePrinting.ScorePages paginas = new ScorePrinting.ScorePages(larga, A4, PrintSettings.everything(total));
        PageFormat papel = pageFormatOf(ScoreSheets.pageSize(Zoom.whole(), A4));

        RecordingCanvas hoja1 = lienzoDeLaHojaImpresa(paginas, papel, 0);
        RecordingCanvas hoja2 = lienzoDeLaHojaImpresa(paginas, papel, 1);

        assertFalse(hoja1.matches(hoja2), "la hoja 2 no puede salir igual a la 1");
    }

    @Test
    void elRangoDePaginasElegidoSeRespetaYNoElResto() {
        Score score = scoreWithMeasures(40);
        int total = ScoreSheets.pageCount(score, A4);
        assertTrue(total >= 4, "hace falta una partitura de varias hojas para probar un rango angosto");

        PrintSettings soloDeLaDosALaTres = PrintSettings.of(2, 3, total, 100, false);
        ScorePrinting.ScorePages paginas = new ScorePrinting.ScorePages(score, A4, soloDeLaDosALaTres);
        PageFormat papel = pageFormatOf(ScoreSheets.pageSize(Zoom.whole(), A4));

        RecordingCanvas primeraQueSale = new RecordingCanvas();
        RecordingCanvas segundaQueSale = new RecordingCanvas();
        assertEquals(Printable.PAGE_EXISTS, paginas.print(primeraQueSale, papel, 0));
        assertEquals(Printable.PAGE_EXISTS, paginas.print(segundaQueSale, papel, 1));
        assertEquals(Printable.NO_SUCH_PAGE, imprimirEnLienzo(paginas, papel, 2),
                "el rango pide dos hojas nada mas");

        assertTrue(
                lienzoDeLaHojaReal(score, 1).matches(primeraQueSale),
                "lo primero que imprime el rango 2-3 tiene que ser la hoja 2 real de la partitura, no la 1");
        assertTrue(
                lienzoDeLaHojaReal(score, 2).matches(segundaQueSale),
                "lo segundo que imprime el rango 2-3 tiene que ser la hoja 3 real de la partitura");
    }

    @Test
    void laEscalaElegidaSeAplicaYDejaEntrarMasHojaEnElMismoPapel() {
        Score score = Score.blank();
        BufferedImage hojaCompleta = ScoreSheets.renderPage(score, Zoom.whole(), A4, 0);
        int filaDelPie = lastInkRowOf(hojaCompleta);
        Dimension sheet = ScoreSheets.pageSize(Zoom.whole(), A4);
        PageFormat papelChico = pageFormatOf(sheet.width, filaDelPie - 100);

        ScorePrinting.ScorePages al100 = new ScorePrinting.ScorePages(
                score, A4, PrintSettings.of(1, 1, 1, 100, false));
        ScorePrinting.ScorePages al50 = new ScorePrinting.ScorePages(
                score, A4, PrintSettings.of(1, 1, 1, 50, false));

        BufferedImage imagenAl100 = blankPage(papelChico);
        BufferedImage imagenAl50 = blankPage(papelChico);
        imprimir(al100, imagenAl100, papelChico, 0);
        imprimir(al50, imagenAl50, papelChico, 0);

        assertFalse(tieneTintaCercaDeLaFila(imagenAl100, filaDelPie, 20, 405),
                "al 100% el pie de pagina no entra en un papel mas chico que la hoja: se pierde");
        assertTrue(tieneTintaCercaDeLaFila(imagenAl50, Math.round(filaDelPie * 0.5f), 20, 405),
                "al 50% la hoja entera -pie de pagina incluido- entra en el mismo papel chico");
    }

    @Test
    void elPageFormatQueDaLaImpresoraSeRespetaYNoElPapelConfiguradoEnLaPartitura() {
        Score score = scoreWithMeasures(4);
        BufferedImage hojaCompleta = ScoreSheets.renderPage(score, Zoom.whole(), A4, 0);
        int filaDelPie = lastInkRowOf(hojaCompleta);
        Dimension sheet = ScoreSheets.pageSize(Zoom.whole(), A4);

        PageFormat papelSinLugarParaElPie = pageFormatOf(sheet.width, filaDelPie - 100);
        PageFormat papelConLugarParaElPie = pageFormatOf(sheet.width, filaDelPie + 20);
        PageFormat lienzoDeSobra = pageFormatOf(sheet.width, sheet.height);

        ScorePrinting.ScorePages paginas = new ScorePrinting.ScorePages(
                score, A4, PrintSettings.of(1, 1, 1, 100, false));

        BufferedImage imagenChica = blankPage(lienzoDeSobra);
        BufferedImage imagenGrande = blankPage(lienzoDeSobra);
        imprimir(paginas, imagenChica, papelSinLugarParaElPie, 0);
        imprimir(paginas, imagenGrande, papelConLugarParaElPie, 0);

        assertFalse(tieneTintaCercaDeLaFila(imagenChica, filaDelPie, 20, sheet.width),
                "el papel chico que da la impresora recorta el pie de pagina");
        assertTrue(tieneTintaCercaDeLaFila(imagenGrande, filaDelPie, 20, sheet.width),
                "el papel grande que da la impresora deja entrar el pie de pagina");
    }

    @Test
    void elDocumentoCentradoCorreElDibujoLaMitadDelSobranteHorizontal() {
        Score score = scoreWithMeasures(4);
        Dimension sheet = ScoreSheets.pageSize(Zoom.whole(), A4);
        int sobranteHorizontal = 200;
        PageFormat papelAncho = pageFormatOf(sheet.width + sobranteHorizontal, sheet.height);

        ScorePrinting.ScorePages sinCentrar = new ScorePrinting.ScorePages(
                score, A4, PrintSettings.of(1, 1, 1, 100, false, false));
        ScorePrinting.ScorePages centrado = new ScorePrinting.ScorePages(
                score, A4, PrintSettings.of(1, 1, 1, 100, false, true));

        BufferedImage imagenSinCentrar = blankPage(papelAncho);
        BufferedImage imagenCentrada = blankPage(papelAncho);
        imprimir(sinCentrar, imagenSinCentrar, papelAncho, 0);
        imprimir(centrado, imagenCentrada, papelAncho, 0);

        int columnaSinCentrar = firstInkColumnOf(imagenSinCentrar);
        int columnaCentrada = firstInkColumnOf(imagenCentrada);

        assertTrue(
                Math.abs((columnaCentrada - columnaSinCentrar) - sobranteHorizontal / 2) <= 3,
                "con 'Documento centrado' tildado el dibujo se tiene que correr la mitad del sobrante horizontal "
                        + "respecto de donde arranca sin centrar");
    }

    private static int imprimirEnLienzo(ScorePrinting.ScorePages paginas, PageFormat format, int pageIndex) {
        return paginas.print(new RecordingCanvas(), format, pageIndex);
    }

    private static RecordingCanvas lienzoDeLaHojaImpresa(ScorePrinting.ScorePages paginas, PageFormat format, int pageIndex) {
        RecordingCanvas canvas = new RecordingCanvas();
        paginas.print(canvas, format, pageIndex);
        return canvas;
    }

    private static RecordingCanvas lienzoDeLaHojaReal(Score score, int page) {
        RecordingCanvas canvas = new RecordingCanvas();
        ScoreSheets.paintPageOn(canvas, score, Zoom.whole(), A4, page);
        return canvas;
    }

    private static int imprimir(
            ScorePrinting.ScorePages paginas, BufferedImage imagen, PageFormat format, int pageIndex) {
        Graphics2D graphics = imagen.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, imagen.getWidth(), imagen.getHeight());
        int result = paginas.print(graphics, format, pageIndex);
        graphics.dispose();
        return result;
    }

    private static BufferedImage blankPage(PageFormat format) {
        return new BufferedImage(
                Math.max(1, (int) Math.round(format.getWidth())),
                Math.max(1, (int) Math.round(format.getHeight())),
                BufferedImage.TYPE_INT_RGB);
    }

    private static PageFormat pageFormatOf(Dimension size) {
        return pageFormatOf(size.width, size.height);
    }

    private static PageFormat pageFormatOf(int width, int height) {
        Paper paper = new Paper();
        paper.setSize(width, height);
        paper.setImageableArea(0, 0, width, height);
        PageFormat format = new PageFormat();
        format.setPaper(paper);
        return format;
    }

    private static int lastInkRowOf(BufferedImage image) {
        for (int y = image.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (esTinta(image.getRGB(x, y))) {
                    return y;
                }
            }
        }
        throw new IllegalStateException("la imagen no tiene tinta en ningun lado");
    }

    private static int firstInkColumnOf(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (esTinta(image.getRGB(x, y))) {
                    return x;
                }
            }
        }
        throw new IllegalStateException("la imagen no tiene tinta en ningun lado");
    }

    private static boolean tieneTintaCercaDeLaFila(BufferedImage imagen, int filaEsperada, int margen, int anchoMaximo) {
        int desde = Math.max(0, filaEsperada - margen);
        int hasta = Math.min(imagen.getHeight(), filaEsperada + margen);
        for (int y = desde; y < hasta; y++) {
            for (int x = 0; x < Math.min(anchoMaximo, imagen.getWidth()); x++) {
                if (esTinta(imagen.getRGB(x, y))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean esTinta(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r + g + b) / 3 < 200;
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
