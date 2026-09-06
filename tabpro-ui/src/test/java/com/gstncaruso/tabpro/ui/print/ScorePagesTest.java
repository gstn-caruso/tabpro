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

/**
 * El {@link Printable} que la impresora de verdad invoca -{@link ScorePrinting.ScorePages}-, sin
 * impresora: se lo llama a mano con un {@link Graphics2D} sacado de un {@link BufferedImage} y un
 * {@link PageFormat} armado en el test. No alcanza con que {@code print(...)} no tire excepcion;
 * se mira cuantas paginas dice que hay y que dibuja en cada una.
 */
class ScorePagesTest {

    private static final PageSetup A4 = PageSetup.defaults();

    @Test
    void unaPartituraCortaTieneUnaSolaHojaYNoSuchPageMarcaElLimite() {
        Score corta = scoreWithMeasures(4);
        int total = ScoreSheets.pageCount(corta, A4);
        assertEquals(1, total, "esta partitura corta tiene que entrar en una sola hoja");

        ScorePrinting.ScorePages paginas = new ScorePrinting.ScorePages(corta, A4, PrintSettings.everything(total));
        PageFormat papel = pageFormatOf(ScoreSheets.pageSize(Zoom.whole(), A4));

        assertEquals(Printable.PAGE_EXISTS, imprimir(paginas, blankPage(papel), papel, 0));
        assertEquals(Printable.NO_SUCH_PAGE, imprimir(paginas, blankPage(papel), papel, 1));
    }

    @Test
    void unaPartituraLargaTieneVariasHojasYNoSuchPageMarcaElLimite() {
        Score larga = scoreWithMeasures(40);
        int total = ScoreSheets.pageCount(larga, A4);
        assertTrue(total > 1, "esta partitura larga tiene que necesitar mas de una hoja");

        ScorePrinting.ScorePages paginas = new ScorePrinting.ScorePages(larga, A4, PrintSettings.everything(total));
        PageFormat papel = pageFormatOf(ScoreSheets.pageSize(Zoom.whole(), A4));

        for (int i = 0; i < total; i++) {
            assertEquals(Printable.PAGE_EXISTS, imprimir(paginas, blankPage(papel), papel, i),
                    "la hoja " + i + " tiene que existir");
        }
        assertEquals(Printable.NO_SUCH_PAGE, imprimir(paginas, blankPage(papel), papel, total),
                "despues de la ultima hoja no puede haber una pagina mas");
    }

    @Test
    void cadaHojaDibujaAlgoDistintoALaAnterior() {
        Score larga = scoreWithMeasures(40);
        int total = ScoreSheets.pageCount(larga, A4);
        ScorePrinting.ScorePages paginas = new ScorePrinting.ScorePages(larga, A4, PrintSettings.everything(total));
        PageFormat papel = pageFormatOf(ScoreSheets.pageSize(Zoom.whole(), A4));

        BufferedImage hoja1 = blankPage(papel);
        BufferedImage hoja2 = blankPage(papel);
        imprimir(paginas, hoja1, papel, 0);
        imprimir(paginas, hoja2, papel, 1);

        assertNotEquals(pixelsOf(hoja1), pixelsOf(hoja2), "la hoja 2 no puede salir igual a la 1");
    }

    @Test
    void elRangoDePaginasElegidoSeRespetaYNoElResto() {
        Score score = scoreWithMeasures(40);
        int total = ScoreSheets.pageCount(score, A4);
        assertTrue(total >= 4, "hace falta una partitura de varias hojas para probar un rango angosto");

        PrintSettings soloDeLaDosALaTres = PrintSettings.of(2, 3, total, 100, false);
        ScorePrinting.ScorePages paginas = new ScorePrinting.ScorePages(score, A4, soloDeLaDosALaTres);
        PageFormat papel = pageFormatOf(ScoreSheets.pageSize(Zoom.whole(), A4));

        BufferedImage primeraQueSale = blankPage(papel);
        BufferedImage segundaQueSale = blankPage(papel);
        assertEquals(Printable.PAGE_EXISTS, imprimir(paginas, primeraQueSale, papel, 0));
        assertEquals(Printable.PAGE_EXISTS, imprimir(paginas, segundaQueSale, papel, 1));
        assertEquals(Printable.NO_SUCH_PAGE, imprimir(paginas, blankPage(papel), papel, 2),
                "el rango pide dos hojas nada mas");

        assertEquals(
                pixelsOf(ScoreSheets.renderPage(score, Zoom.whole(), A4, 1)), pixelsOf(primeraQueSale),
                "lo primero que imprime el rango 2-3 tiene que ser la hoja 2 real de la partitura, no la 1");
        assertEquals(
                pixelsOf(ScoreSheets.renderPage(score, Zoom.whole(), A4, 2)), pixelsOf(segundaQueSale),
                "lo segundo que imprime el rango 2-3 tiene que ser la hoja 3 real de la partitura");
    }

    /**
     * Papel de la impresora mas chico que la hoja: al 100% el pie de pagina -lo ultimo que se
     * dibuja, bien abajo- no entra y se pierde para siempre, porque {@code sheetsToPrint()} no
     * agrega una hoja mas por lo que quedo afuera. Al 50% la hoja entera, pie incluido, entra en
     * el mismo papel. Se usa una partitura en blanco para que lo unico que pueda aparecer ahi
     * abajo sea el pie -"Pagina 1 de 1"-, nunca musica.
     */
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

    /**
     * El mismo papel chico del test de arriba, pero ahora se mueve el {@link PageFormat} de la
     * impresora en vez de la escala: uno deja lugar para el pie de pagina y el otro no. El papel
     * configurado en la partitura ({@link PageSetup}) es el mismo A4 en los dos casos -lo unico
     * que cambia es lo que "da la impresora".
     */
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

    /** Un PageFormat armado a mano, como el que devolveria el dialogo de la impresora. */
    private static PageFormat pageFormatOf(int width, int height) {
        Paper paper = new Paper();
        paper.setSize(width, height);
        paper.setImageableArea(0, 0, width, height);
        PageFormat format = new PageFormat();
        format.setPaper(paper);
        return format;
    }

    /** La ultima fila de la imagen que tiene tinta: en una hoja siempre es el pie de pagina. */
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

    /** Un pixel "es tinta" si es notoriamente mas oscuro que el papel (blanco o el F6F6F2 de la hoja). */
    private static boolean esTinta(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r + g + b) / 3 < 200;
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
