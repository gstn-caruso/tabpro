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
    void aShortScoreHasOnlyOneSheetAndNoSuchPageMarksTheLimit() {
        Score shortScore = scoreWithMeasures(4);
        int total = ScoreSheets.pageCount(shortScore, A4);
        assertEquals(1, total, "esta partitura corta tiene que entrar en una sola hoja");

        ScorePrinting.ScorePages pages = new ScorePrinting.ScorePages(shortScore, A4, PrintSettings.everything(total));
        PageFormat paper = pageFormatOf(ScoreSheets.pageSize(Zoom.whole(), A4));

        assertEquals(Printable.PAGE_EXISTS, printOnANewCanvas(pages, paper, 0));
        assertEquals(Printable.NO_SUCH_PAGE, printOnANewCanvas(pages, paper, 1));
    }

    @Test
    void aLongScoreHasSeveralSheetsAndNoSuchPageMarksTheLimit() {
        Score longScore = scoreWithMeasures(40);
        int total = ScoreSheets.pageCount(longScore, A4);
        assertTrue(total > 1, "esta partitura larga tiene que necesitar mas de una hoja");

        ScorePrinting.ScorePages pages = new ScorePrinting.ScorePages(longScore, A4, PrintSettings.everything(total));
        PageFormat paper = pageFormatOf(ScoreSheets.pageSize(Zoom.whole(), A4));

        for (int i = 0; i < total; i++) {
            assertEquals(Printable.PAGE_EXISTS, printOnANewCanvas(pages, paper, i),
                    "la hoja " + i + " tiene que existir");
        }
        assertEquals(Printable.NO_SUCH_PAGE, printOnANewCanvas(pages, paper, total),
                "despues de la ultima hoja no puede haber una pagina mas");
    }

    @Test
    void eachSheetDrawsSomethingDifferentFromThePrevious() {
        Score longScore = scoreWithMeasures(40);
        int total = ScoreSheets.pageCount(longScore, A4);
        ScorePrinting.ScorePages pages = new ScorePrinting.ScorePages(longScore, A4, PrintSettings.everything(total));
        PageFormat paper = pageFormatOf(ScoreSheets.pageSize(Zoom.whole(), A4));

        RecordingCanvas sheet1 = canvasOfThePrintedSheet(pages, paper, 0);
        RecordingCanvas sheet2 = canvasOfThePrintedSheet(pages, paper, 1);

        assertFalse(sheet1.matches(sheet2), "la hoja 2 no puede salir igual a la 1");
    }

    @Test
    void theChosenPageRangeIsRespectedAndNotTheRest() {
        Score score = scoreWithMeasures(40);
        int total = ScoreSheets.pageCount(score, A4);
        assertTrue(total >= 4, "hace falta una partitura de varias hojas para probar un rango angosto");

        PrintSettings onlyFromPageTwoToThree = PrintSettings.of(2, 3, total, 100, false);
        ScorePrinting.ScorePages pages = new ScorePrinting.ScorePages(score, A4, onlyFromPageTwoToThree);
        PageFormat paper = pageFormatOf(ScoreSheets.pageSize(Zoom.whole(), A4));

        RecordingCanvas firstPrinted = new RecordingCanvas();
        RecordingCanvas secondPrinted = new RecordingCanvas();
        assertEquals(Printable.PAGE_EXISTS, pages.print(firstPrinted, paper, 0));
        assertEquals(Printable.PAGE_EXISTS, pages.print(secondPrinted, paper, 1));
        assertEquals(Printable.NO_SUCH_PAGE, printOnANewCanvas(pages, paper, 2),
                "el rango pide dos hojas nada mas");

        assertTrue(
                canvasOfTheActualSheet(score, 1).matches(firstPrinted),
                "lo primero que imprime el rango 2-3 tiene que ser la hoja 2 real de la partitura, no la 1");
        assertTrue(
                canvasOfTheActualSheet(score, 2).matches(secondPrinted),
                "lo segundo que imprime el rango 2-3 tiene que ser la hoja 3 real de la partitura");
    }

    @Test
    void theChosenScaleIsAppliedAndFitsMoreSheetOnTheSamePaper() {
        Score score = Score.blank();
        BufferedImage fullSheet = ScoreSheets.renderPage(score, Zoom.whole(), A4, 0);
        int footerRow = lastInkRowOf(fullSheet);
        Dimension sheet = ScoreSheets.pageSize(Zoom.whole(), A4);
        PageFormat smallPaper = pageFormatOf(sheet.width, footerRow - 100);

        ScorePrinting.ScorePages at100 = new ScorePrinting.ScorePages(
                score, A4, PrintSettings.of(1, 1, 1, 100, false));
        ScorePrinting.ScorePages at50 = new ScorePrinting.ScorePages(
                score, A4, PrintSettings.of(1, 1, 1, 50, false));

        BufferedImage imageAt100 = blankPage(smallPaper);
        BufferedImage imageAt50 = blankPage(smallPaper);
        printInto(at100, imageAt100, smallPaper, 0);
        printInto(at50, imageAt50, smallPaper, 0);

        assertFalse(hasInkNearTheRow(imageAt100, footerRow, 20, 405),
                "al 100% el pie de pagina no entra en un papel mas chico que la hoja: se pierde");
        assertTrue(hasInkNearTheRow(imageAt50, Math.round(footerRow * 0.5f), 20, 405),
                "al 50% la hoja entera -pie de pagina incluido- entra en el mismo papel chico");
    }

    @Test
    void thePageFormatFromThePrinterIsRespectedNotThePaperConfiguredInTheScore() {
        Score score = scoreWithMeasures(4);
        BufferedImage fullSheet = ScoreSheets.renderPage(score, Zoom.whole(), A4, 0);
        int footerRow = lastInkRowOf(fullSheet);
        Dimension sheet = ScoreSheets.pageSize(Zoom.whole(), A4);

        PageFormat paperWithNoRoomForTheFooter = pageFormatOf(sheet.width, footerRow - 100);
        PageFormat paperWithRoomForTheFooter = pageFormatOf(sheet.width, footerRow + 20);
        PageFormat roomyPaper = pageFormatOf(sheet.width, sheet.height);

        ScorePrinting.ScorePages pages = new ScorePrinting.ScorePages(
                score, A4, PrintSettings.of(1, 1, 1, 100, false));

        BufferedImage smallImage = blankPage(roomyPaper);
        BufferedImage bigImage = blankPage(roomyPaper);
        printInto(pages, smallImage, paperWithNoRoomForTheFooter, 0);
        printInto(pages, bigImage, paperWithRoomForTheFooter, 0);

        assertFalse(hasInkNearTheRow(smallImage, footerRow, 20, sheet.width),
                "el papel chico que da la impresora recorta el pie de pagina");
        assertTrue(hasInkNearTheRow(bigImage, footerRow, 20, sheet.width),
                "el papel grande que da la impresora deja entrar el pie de pagina");
    }

    @Test
    void centeringTheDocumentShiftsTheDrawingHalfTheHorizontalSlack() {
        Score score = scoreWithMeasures(4);
        Dimension sheet = ScoreSheets.pageSize(Zoom.whole(), A4);
        int horizontalSlack = 200;
        PageFormat widePaper = pageFormatOf(sheet.width + horizontalSlack, sheet.height);

        ScorePrinting.ScorePages notCentered = new ScorePrinting.ScorePages(
                score, A4, PrintSettings.of(1, 1, 1, 100, false, false));
        ScorePrinting.ScorePages centered = new ScorePrinting.ScorePages(
                score, A4, PrintSettings.of(1, 1, 1, 100, false, true));

        BufferedImage imageNotCentered = blankPage(widePaper);
        BufferedImage centeredImage = blankPage(widePaper);
        printInto(notCentered, imageNotCentered, widePaper, 0);
        printInto(centered, centeredImage, widePaper, 0);

        int columnNotCentered = firstInkColumnOf(imageNotCentered);
        int centeredColumn = firstInkColumnOf(centeredImage);

        assertTrue(
                Math.abs((centeredColumn - columnNotCentered) - horizontalSlack / 2) <= 3,
                "con 'Documento centrado' tildado el dibujo se tiene que correr la mitad del sobrante horizontal "
                        + "respecto de donde arranca sin centrar");
    }

    private static int printOnANewCanvas(ScorePrinting.ScorePages pages, PageFormat format, int pageIndex) {
        return pages.print(new RecordingCanvas(), format, pageIndex);
    }

    private static RecordingCanvas canvasOfThePrintedSheet(ScorePrinting.ScorePages pages, PageFormat format, int pageIndex) {
        RecordingCanvas canvas = new RecordingCanvas();
        pages.print(canvas, format, pageIndex);
        return canvas;
    }

    private static RecordingCanvas canvasOfTheActualSheet(Score score, int page) {
        RecordingCanvas canvas = new RecordingCanvas();
        ScoreSheets.paintPageOn(canvas, score, Zoom.whole(), A4, page);
        return canvas;
    }

    private static int printInto(
            ScorePrinting.ScorePages pages, BufferedImage image, PageFormat format, int pageIndex) {
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        int result = pages.print(graphics, format, pageIndex);
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
                if (isInk(image.getRGB(x, y))) {
                    return y;
                }
            }
        }
        throw new IllegalStateException("la imagen no tiene tinta en ningun lado");
    }

    private static int firstInkColumnOf(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (isInk(image.getRGB(x, y))) {
                    return x;
                }
            }
        }
        throw new IllegalStateException("la imagen no tiene tinta en ningun lado");
    }

    private static boolean hasInkNearTheRow(BufferedImage image, int expectedRow, int margin, int maxWidth) {
        int from = Math.max(0, expectedRow - margin);
        int to = Math.min(image.getHeight(), expectedRow + margin);
        for (int y = from; y < to; y++) {
            for (int x = 0; x < Math.min(maxWidth, image.getWidth()); x++) {
                if (isInk(image.getRGB(x, y))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isInk(int rgb) {
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
