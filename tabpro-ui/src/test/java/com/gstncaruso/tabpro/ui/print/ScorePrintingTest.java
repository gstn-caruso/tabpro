package com.gstncaruso.tabpro.ui.print;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.files.ScoreFileException;
import com.gstncaruso.tabpro.core.files.ScoreFileProblem;
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
    void acceptingTheDialogSendsTheJobAndThePrintableThatDrawsTheActualScore() throws PrinterException {
        Score score = scoreWithMeasures(4);
        PrintSettings settings = PrintSettings.everything(ScoreSheets.pageCount(score, A4));
        RecordingPrinting printing = new RecordingPrinting();
        ScorePrinting scorePrinting = new ScorePrinting(printing);

        scorePrinting.print(score, A4, settings, "my-score.tab");

        assertEquals("my-score.tab", printing.jobName());
        assertTrue(printing.printCalled(), "accepting the dialog has to actually reach the print call");
        assertEquals(
                new ScorePrinting.ScorePages(score, A4, settings), printing.printable(),
                "the received Printable has to be the one that paints this real score (see ScorePagesTest)");
    }

    @Test
    void ifThePrintDialogIsCancelledItNeverActuallyPrints() throws PrinterException {
        Score score = scoreWithMeasures(4);
        RecordingPrinting printing = new RecordingPrinting();
        printing.cancelPrintDialog();
        ScorePrinting scorePrinting = new ScorePrinting(printing);

        scorePrinting.print(score, A4, PrintSettings.everything(1), "my-score.tab");

        assertFalse(printing.printCalled(), "cancelling the dialog must never actually reach the print call");
    }

    @Test
    void thePageFormatChosenWhileConfiguringIsKeptForTheNextPrint() throws PrinterException {
        Score score = scoreWithMeasures(4);
        RecordingPrinting printing = new RecordingPrinting();
        PageFormat chosen = new PageFormat();
        printing.chooseInPageDialog(chosen);
        ScorePrinting scorePrinting = new ScorePrinting(printing);

        scorePrinting.configurePrinterPage();
        scorePrinting.print(score, A4, PrintSettings.everything(1), "my-score.tab");

        assertSame(chosen, printing.printableFormat(),
                "the PageFormat chosen in Configure has to be the one used in the next print");
    }

    @Tag("integration")
    @Test
    void exportsAnActualBmpInPageMode(@TempDir Path tempDir) throws IOException {
        Score score = scoreWithMeasures(4);
        Path path = tempDir.resolve("score.bmp");

        ScorePrinting.exportImage(score, A4, path, ViewMode.PAGE, Zoom.whole());

        assertTrue(Files.exists(path));
        BufferedImage expected = ScoreSheets.render(score, Zoom.whole(), A4);
        BufferedImage read = ImageIO.read(path.toFile());

        assertEquals(expected.getWidth(), read.getWidth());
        assertEquals(expected.getHeight(), read.getHeight());
        assertEquals(pixelsOf(expected), pixelsOf(read), "the bmp has to look the same as the in-memory render");
        assertTrue(distinctColorsOf(read).size() > 1, "the image cannot come out as a single color");
    }

    @Tag("integration")
    @Test
    void theExportedBmpIsIdenticalToWhatBmpDocumentWrites(@TempDir Path tempDir) throws IOException {
        Score score = scoreWithMeasures(4);
        Path path = tempDir.resolve("score.bmp");

        ScorePrinting.exportImage(score, A4, path, ViewMode.PAGE, Zoom.whole());

        java.io.ByteArrayOutputStream expected = new java.io.ByteArrayOutputStream();
        BmpDocument.writeTo(ScoreSheets.render(score, Zoom.whole(), A4), expected);
        assertArrayEquals(expected.toByteArray(), Files.readAllBytes(path),
                "the exported bmp has to be the one BmpDocument writes, not another codec");
    }

    @Test
    void bmpOutsidePageModeWarnsAndWritesNothing(@TempDir Path tempDir) {
        Score score = scoreWithMeasures(4);
        Path path = tempDir.resolve("score.bmp");

        ImageExportException error = assertThrows(ImageExportException.class,
                () -> ScorePrinting.exportImage(score, A4, path, ViewMode.SCREEN_VERTICAL, Zoom.whole()));

        assertTrue(error.getMessage().toLowerCase(java.util.Locale.ROOT).contains("bmp"));
        assertFalse(Files.exists(path), "no half-written file can be left behind");
    }

    @Tag("integration")
    @Test
    void pngOutsidePageModeExportsWithoutIssue(@TempDir Path tempDir) {
        Score score = scoreWithMeasures(4);
        Path path = tempDir.resolve("score.png");

        ScorePrinting.exportImage(score, A4, path, ViewMode.SCREEN_VERTICAL, Zoom.whole());

        assertTrue(Files.exists(path), "the restriction is only for bmp");
    }

    @Tag("integration")
    @Test
    void exportsTheImageWithTheWindowsZoom(@TempDir Path tempDir) throws IOException {
        Score score = scoreWithMeasures(4);
        Path at100 = tempDir.resolve("at-100.png");
        Path at200 = tempDir.resolve("at-200.png");

        ScorePrinting.exportImage(score, A4, at100, ViewMode.PAGE, new Zoom(100));
        ScorePrinting.exportImage(score, A4, at200, ViewMode.PAGE, new Zoom(200));

        BufferedImage imageAt100 = ImageIO.read(at100.toFile());
        BufferedImage imageAt200 = ImageIO.read(at200.toFile());

        assertNotEquals(imageAt100.getWidth(), imageAt200.getWidth(),
                "the same score at 100% and at 200% cannot give the same width in pixels");
        assertNotEquals(imageAt100.getHeight(), imageAt200.getHeight(),
                "the same score at 100% and at 200% cannot give the same height in pixels");
    }

    @Tag("integration")
    @Test
    void exportsTheImageInParchmentModeWithoutPageBreaks(@TempDir Path tempDir) throws IOException {
        Score score = scoreWithMeasures(16);
        Path pagePath = tempDir.resolve("page.png");
        Path parchmentPath = tempDir.resolve("parchment.png");

        ScorePrinting.exportImage(score, A4, pagePath, ViewMode.PAGE, Zoom.whole());
        ScorePrinting.exportImage(score, A4, parchmentPath, ViewMode.PARCHMENT, Zoom.whole());

        BufferedImage pageImage = ImageIO.read(pagePath.toFile());
        BufferedImage parchmentImage = ImageIO.read(parchmentPath.toFile());

        assertNotEquals(pageImage.getHeight(), parchmentImage.getHeight(),
                "in parchment there are no page breaks: the height has to differ from Page mode");
    }

    @Test
    void anImageThatImageIoCannotEncodeAsBmpWarnsInsteadOfStayingSilent(@TempDir Path tempDir) {
        Path path = tempDir.resolve("cannot-be-written.bmp");
        BufferedImage imageWithRealAlpha = imageWithRealTransparency();

        ImageExportException error = assertThrows(ImageExportException.class,
                () -> ScorePrinting.writeImage(imageWithRealAlpha, "bmp", path));

        assertTrue(error.getMessage().toLowerCase(java.util.Locale.ROOT).contains("bmp"),
                "the message has to say which format failed");
        assertFalse(Files.exists(path), "if ImageIO could not write anything, no file can be left behind");
    }

    @Test
    void anImageThatImageIoCannotEncodeAsJpgWarnsInsteadOfStayingSilent(@TempDir Path tempDir) {
        Path path = tempDir.resolve("cannot-be-written.jpg");
        BufferedImage imageWithRealAlpha = imageWithRealTransparency();

        ImageExportException error = assertThrows(ImageExportException.class,
                () -> ScorePrinting.writeImage(imageWithRealAlpha, "jpg", path));

        assertTrue(error.getMessage().toLowerCase(java.util.Locale.ROOT).contains("jpg"),
                "the message has to say which format failed");
        assertFalse(Files.exists(path), "if ImageIO could not write anything, no file can be left behind");
    }

    @Test
    void aPdfThatCannotBeWrittenIsReportedWithItsPath(@TempDir Path tempDir) {
        Path path = tempDir.resolve("missing-folder").resolve("score.pdf");

        ScoreFileException failure = assertThrows(
                ScoreFileException.class, () -> ScorePrinting.exportPdf(scoreWithMeasures(1), A4, path));

        assertEquals(ScoreFileProblem.CANNOT_WRITE, failure.problem());
        assertEquals(List.of(path), failure.arguments());
    }

    @Test
    void anImageThatCannotBeWrittenIsReportedWithItsPath(@TempDir Path tempDir) {
        Path path = tempDir.resolve("missing-folder").resolve("score.bmp");
        BufferedImage opaque = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);

        ScoreFileException failure =
                assertThrows(ScoreFileException.class, () -> ScorePrinting.writeImage(opaque, "bmp", path));

        assertEquals(ScoreFileProblem.CANNOT_WRITE, failure.problem());
        assertEquals(List.of(path), failure.arguments());
    }

    private static BufferedImage imageWithRealTransparency() {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0x80FF0000);
        return image;
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
