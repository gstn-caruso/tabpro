package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.dispatchKeyAndDetectDialog;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithMeasures;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButton;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.dialogs.pagesetup.PageSetupPanel;
import com.gstncaruso.tabpro.ui.dialogs.print.PrintPanel;
import com.gstncaruso.tabpro.ui.page.DefaultPageSetup;
import com.gstncaruso.tabpro.ui.page.PageSetup;
import com.gstncaruso.tabpro.ui.print.PrintSettings;
import com.gstncaruso.tabpro.ui.print.ScorePrinting;
import com.gstncaruso.tabpro.ui.print.ScoreSheets;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import com.gstncaruso.tabpro.ui.score.Zoom;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import javax.swing.JSpinner;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * The real AWT {@code PrinterJob} has no seam: {@link ScorePrinting#print} and
 * {@link ScorePrinting#configurePrinterPage} call {@code PrinterJob.getPrinterJob()} directly,
 * and that call opens the operating system's <em>native</em> dialog, not a Swing
 * {@code JDialog}, which also depends on some print service being installed. That is why this
 * class never presses "Print" or "Configure…" in the real dialog: it verifies the whole path up
 * to there (that it opens with the real score's data, that its real controls reflect what is
 * chosen) and then closes with Cancel, the only way to exercise the path without risking the
 * process waiting on a native window that nobody will handle.
 */
@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class PrintAuditTest {

    @Test
    void printingThroughTheMenuOpensTheRealDialogWithTheScoresSheets() throws Exception {
        Editor editor = editorWithMeasures(60);
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Imprimir…");
            assertNotNull(item, "could not find 'Imprimir…' in the real menu");
            assertEquals(KeyStroke.getKeyStroke("ctrl P"), item.getAccelerator());

            int sheetCount = ScorePrinting.pageCount(
                    editor.score(), com.gstncaruso.tabpro.ui.page.DefaultPageSetup.userSetup().get());
            assertTrue(sheetCount >= 1);

            withDialog(item::doClick, dialog -> {
                PrintPanel panel = findComponent(dialog, PrintPanel.class);
                assertNotNull(panel, "could not find the real PrintPanel inside the dialog");
                assertEquals(
                        PrintSettings.everything(sheetCount), panel.toPrintSettings(),
                        "by default the whole score must come out, with the real sheet count");

                panel.printOnly(1, 1);
                assertEquals(
                        PrintSettings.of(1, 1, sheetCount, 100, false), panel.toPrintSettings(),
                        "the 'Paginas' radio and the real spinners must reach PrintSettings");

                panel.fitToPage();
                java.util.List<JSpinner> spinners = AuditSupport.findComponents(dialog, JSpinner.class);
                JSpinner scalePercent = spinners.get(2);
                assertFalse(scalePercent.isEnabled(), "with 'Ajustar a la hoja' checked, the real scale is not editable");

                panel.scaleTo(150);
                assertTrue(scalePercent.isEnabled(), "going back to a fixed scale re-enables the real spinner");
                assertEquals(150, panel.toPrintSettings().scalePercent());

                findButton(dialog, "Cancelar").doClick();
            });
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void pressingPrintReachesTheFakePrinterJobWithTheChosenRangeAndRealScore() throws Exception {
        Editor editor = editorWithMeasures(60);
        AuditSupport.RecordingPrinting printing = new AuditSupport.RecordingPrinting();
        MainFrame frame = newFrame(editor, printing);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Imprimir…");
            int sheetCount = ScorePrinting.pageCount(editor.score(), DefaultPageSetup.userSetup().get());
            assertTrue(sheetCount > 1, "a multi-sheet score is needed to choose a narrow range");
            int from = 2;

            withDialog(item::doClick, dialog -> {
                PrintPanel panel = findComponent(dialog, PrintPanel.class);
                panel.printOnly(from, sheetCount);
                findButton(dialog, "Imprimir").doClick();
            });

            assertTrue(printing.printCalled(), "pressing Imprimir must really reach the (fake) PrinterJob");
            assertNotNull(printing.jobName());
            assertNotNull(printing.printable(), "the real Printable of the score must arrive");

            int pagesInRange = sheetCount - from + 1;
            PrintResult lastInRange = printOnLargeCanvas(printing.printable(), pagesInRange - 1);
            assertEquals(Printable.PAGE_EXISTS, lastInRange.pageResult(),
                    "the last page of the chosen range must exist");
            assertTrue(hasInk(lastInRange.canvas()),
                    "the received Printable must paint the real score, not stay blank");

            PrintResult outsideRange = printOnLargeCanvas(printing.printable(), pagesInRange);
            assertEquals(Printable.NO_SUCH_PAGE, outsideRange.pageResult(),
                    "the range chosen in the real dialog must be the one that reaches the Printable, not the whole score");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    private record PrintResult(int pageResult, BufferedImage canvas) {
    }

    private static PrintResult printOnLargeCanvas(Printable printable, int pageIndex) throws java.awt.print.PrinterException {
        Paper paper = new Paper();
        paper.setSize(5000, 7000);
        paper.setImageableArea(0, 0, 5000, 7000);
        PageFormat format = new PageFormat();
        format.setPaper(paper);

        BufferedImage canvas = new BufferedImage(5000, 7000, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        graphics.setColor(java.awt.Color.WHITE);
        graphics.fillRect(0, 0, 5000, 7000);
        int result = printable.print(graphics, format, pageIndex);
        graphics.dispose();
        return new PrintResult(result, canvas);
    }

    private static boolean hasInk(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y += 5) {
            for (int x = 0; x < image.getWidth(); x += 5) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if ((r + g + b) / 3 < 200) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void checkingCenteredDocumentShiftsTheRealPrintableSheetByHalfTheHorizontalSlack() throws Exception {
        Editor editor = editorWithMeasures(4);
        AuditSupport.RecordingPrinting printing = new AuditSupport.RecordingPrinting();
        MainFrame frame = newFrame(editor, printing);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Imprimir…");

            withDialog(item::doClick, dialog -> findButton(dialog, "Imprimir").doClick());
            Printable uncentered = printing.printable();
            assertNotNull(uncentered, "the real Printable of the score must arrive");

            withDialog(item::doClick, dialog -> {
                PrintPanel panel = findComponent(dialog, PrintPanel.class);
                assertFalse(panel.toPrintSettings().centeredDocument(),
                        "by default the real document does not start centered");

                panel.centerDocument();

                assertTrue(panel.toPrintSettings().centeredDocument(),
                        "checking the real 'Documento centrado' checkbox must reach the real options");
                findButton(dialog, "Imprimir").doClick();
            });
            Printable centered = printing.printable();
            assertNotNull(centered, "the real Printable must arrive, already centered");

            PageSetup setup = DefaultPageSetup.userSetup().get();
            Dimension sheet = ScoreSheets.pageSize(Zoom.whole(), setup);
            int horizontalSlack = 200;

            int uncenteredColumn = firstInkColumnOf(printOnPaper(
                    uncentered, sheet.width + horizontalSlack, sheet.height));
            int centeredColumn = firstInkColumnOf(printOnPaper(
                    centered, sheet.width + horizontalSlack, sheet.height));

            assertTrue(
                    Math.abs((centeredColumn - uncenteredColumn) - horizontalSlack / 2) <= 5,
                    "with 'Documento centrado' checked from the real dialog, the fake PrinterJob must "
                            + "receive the sheet shifted half the horizontal slack");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    private static BufferedImage printOnPaper(Printable printable, int width, int height)
            throws java.awt.print.PrinterException {
        Paper paper = new Paper();
        paper.setSize(width, height);
        paper.setImageableArea(0, 0, width, height);
        PageFormat format = new PageFormat();
        format.setPaper(paper);

        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        graphics.setColor(java.awt.Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        printable.print(graphics, format, 0);
        graphics.dispose();
        return canvas;
    }

    private static int firstInkColumnOf(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if ((r + g + b) / 3 < 200) {
                    return x;
                }
            }
        }
        throw new IllegalStateException("the image has no ink anywhere");
    }

    @Test
    void ctrlPWithTheScoreFocusedOpensTheSameRealDialogAsTheMenu() throws Exception {
        Editor editor = editorWithMeasures(4);
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            assertNotNull(canvas, "could not find the real ScoreCanvas");

            boolean openedADialog = dispatchKeyAndDetectDialog(canvas, KeyStroke.getKeyStroke("ctrl P"), 2000);

            assertTrue(openedADialog, "Ctrl+P with the score focused must open the real Imprimir dialog");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void pageSetupThroughTheMenuOpensTheRealDialogAndTheChosenSizeStaysApplied() throws Exception {
        Editor editor = editorWithMeasures(4);
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Configurar página…");
            assertNotNull(item, "could not find 'Configurar página…' in the real menu");
            assertEquals(KeyStroke.getKeyStroke("F8"), item.getAccelerator());

            withDialog(item::doClick, dialog -> {
                PageSetupPanel panel = findComponent(dialog, PageSetupPanel.class);
                assertNotNull(panel, "could not find the real PageSetupPanel inside the dialog");
                PageSetup current = panel.toPageSetup();
                PageSetup changed = new PageSetup(
                        current.paperFormat(), current.orientation(), current.marginTop(), current.marginBottom(),
                        current.marginLeft(), current.marginRight(), 150, current.header(), current.footer());
                assertNotEquals(current, changed);

                panel.apply(changed);
                assertEquals(changed, panel.toPageSetup(), "applying the change must be reflected in the real fields");

                findButton(dialog, "Aceptar").doClick();
            });

            withDialog(item::doClick, dialog -> {
                PageSetupPanel reopened = findComponent(dialog, PageSetupPanel.class);
                assertEquals(
                        150, reopened.toPageSetup().scorePercent(),
                        "the score size chosen in the real dialog must still be applied when reopened");

                findButton(dialog, "Cancelar").doClick();
            });
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
