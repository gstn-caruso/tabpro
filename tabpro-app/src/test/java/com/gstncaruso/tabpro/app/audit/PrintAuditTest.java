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
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class PrintAuditTest {

    @Test
    void imprimirPorElMenuAbreElDialogoRealConLasHojasDeLaPartituraReal() throws Exception {
        Editor editor = editorWithMeasures(60);
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Imprimir…");
            assertNotNull(item, "no encontre 'Imprimir…' en el menu real");
            assertEquals(KeyStroke.getKeyStroke("ctrl P"), item.getAccelerator());

            int sheetCount = ScorePrinting.pageCount(
                    editor.score(), com.gstncaruso.tabpro.ui.page.DefaultPageSetup.userSetup().get());
            assertTrue(sheetCount >= 1);

            withDialog(item::doClick, dialog -> {
                PrintPanel panel = findComponent(dialog, PrintPanel.class);
                assertNotNull(panel, "no encontre el PrintPanel real dentro del dialogo");
                assertEquals(
                        PrintSettings.everything(sheetCount), panel.toPrintSettings(),
                        "por defecto tiene que salir toda la partitura, con la cantidad real de hojas");

                panel.printOnly(1, 1);
                assertEquals(
                        PrintSettings.of(1, 1, sheetCount, 100, false), panel.toPrintSettings(),
                        "el radio 'Paginas' y los spinners reales tienen que llegar a PrintSettings");

                panel.fitToPage();
                java.util.List<JSpinner> spinners = AuditSupport.findComponents(dialog, JSpinner.class);
                JSpinner scalePercent = spinners.get(2);
                assertFalse(scalePercent.isEnabled(), "con 'Ajustar a la hoja' marcado, la escala real no se edita");

                panel.scaleTo(150);
                assertTrue(scalePercent.isEnabled(), "al volver a una escala fija, el spinner real se vuelve a habilitar");
                assertEquals(150, panel.toPrintSettings().scalePercent());

                findButton(dialog, "Cancelar").doClick();
            });
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void alApretarImprimirLlegaAlPrinterJobFalsoConElRangoYLaPartituraRealElegidos() throws Exception {
        Editor editor = editorWithMeasures(60);
        AuditSupport.RecordingPrinting printing = new AuditSupport.RecordingPrinting();
        MainFrame frame = newFrame(editor, printing);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Imprimir…");
            int sheetCount = ScorePrinting.pageCount(editor.score(), DefaultPageSetup.userSetup().get());
            assertTrue(sheetCount > 1, "hace falta una partitura de varias hojas para elegir un rango angosto");
            int desde = 2;

            withDialog(item::doClick, dialog -> {
                PrintPanel panel = findComponent(dialog, PrintPanel.class);
                panel.printOnly(desde, sheetCount);
                findButton(dialog, "Imprimir").doClick();
            });

            assertTrue(printing.printCalled(), "apretar Imprimir tiene que llegar de verdad al PrinterJob (falso)");
            assertNotNull(printing.jobName());
            assertNotNull(printing.printable(), "tiene que llegar el Printable real de la partitura");

            int paginasEnElRango = sheetCount - desde + 1;
            PrintResult ultimaDelRango = printOnLargeCanvas(printing.printable(), paginasEnElRango - 1);
            assertEquals(Printable.PAGE_EXISTS, ultimaDelRango.pageResult(),
                    "la ultima pagina del rango elegido tiene que existir");
            assertTrue(hasInk(ultimaDelRango.canvas()),
                    "el Printable recibido tiene que pintar la partitura real, no quedar en blanco");

            PrintResult fueraDelRango = printOnLargeCanvas(printing.printable(), paginasEnElRango);
            assertEquals(Printable.NO_SUCH_PAGE, fueraDelRango.pageResult(),
                    "el rango elegido en el dialogo real tiene que ser el que llega al Printable, no la partitura entera");
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
    void alTildarDocumentoCentradoElPrintableRealCorreLaHojaLaMitadDelSobranteHorizontal() throws Exception {
        Editor editor = editorWithMeasures(4);
        AuditSupport.RecordingPrinting printing = new AuditSupport.RecordingPrinting();
        MainFrame frame = newFrame(editor, printing);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Imprimir…");

            withDialog(item::doClick, dialog -> findButton(dialog, "Imprimir").doClick());
            Printable sinCentrar = printing.printable();
            assertNotNull(sinCentrar, "tiene que llegar el Printable real de la partitura");

            withDialog(item::doClick, dialog -> {
                PrintPanel panel = findComponent(dialog, PrintPanel.class);
                assertFalse(panel.toPrintSettings().centeredDocument(),
                        "por defecto el documento real no arranca centrado");

                panel.centerDocument();

                assertTrue(panel.toPrintSettings().centeredDocument(),
                        "tildar el casillero real 'Documento centrado' tiene que llegar a las opciones reales");
                findButton(dialog, "Imprimir").doClick();
            });
            Printable centrado = printing.printable();
            assertNotNull(centrado, "tiene que llegar el Printable real, ya centrado");

            PageSetup setup = DefaultPageSetup.userSetup().get();
            Dimension sheet = ScoreSheets.pageSize(Zoom.whole(), setup);
            int sobranteHorizontal = 200;

            int columnaSinCentrar = firstInkColumnOf(printOnPaper(
                    sinCentrar, sheet.width + sobranteHorizontal, sheet.height));
            int columnaCentrada = firstInkColumnOf(printOnPaper(
                    centrado, sheet.width + sobranteHorizontal, sheet.height));

            assertTrue(
                    Math.abs((columnaCentrada - columnaSinCentrar) - sobranteHorizontal / 2) <= 5,
                    "con 'Documento centrado' tildado desde el dialogo real, el PrinterJob falso tiene que "
                            + "recibir la hoja corrida la mitad del sobrante horizontal");
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
        throw new IllegalStateException("la imagen no tiene tinta en ningun lado");
    }

    @Test
    void ctrlPConLaPartituraEnfocadaAbreElMismoDialogoRealQueElMenu() throws Exception {
        Editor editor = editorWithMeasures(4);
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            assertNotNull(canvas, "no encontre el ScoreCanvas real");

            boolean abrioUnDialogo = dispatchKeyAndDetectDialog(canvas, KeyStroke.getKeyStroke("ctrl P"), 2000);

            assertTrue(abrioUnDialogo, "Ctrl+P con la partitura enfocada tiene que abrir el dialogo real de Imprimir");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void configurarPaginaPorElMenuAbreElDialogoRealYElTamanoElegidoQuedaAplicado() throws Exception {
        Editor editor = editorWithMeasures(4);
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Configurar página…");
            assertNotNull(item, "no encontre 'Configurar página…' en el menu real");
            assertEquals(KeyStroke.getKeyStroke("F8"), item.getAccelerator());

            withDialog(item::doClick, dialog -> {
                PageSetupPanel panel = findComponent(dialog, PageSetupPanel.class);
                assertNotNull(panel, "no encontre el PageSetupPanel real dentro del dialogo");
                PageSetup current = panel.toPageSetup();
                PageSetup changed = new PageSetup(
                        current.paperFormat(), current.orientation(), current.marginTop(), current.marginBottom(),
                        current.marginLeft(), current.marginRight(), 150, current.header(), current.footer());
                assertNotEquals(current, changed);

                panel.apply(changed);
                assertEquals(changed, panel.toPageSetup(), "aplicar el cambio tiene que reflejarse en los campos reales");

                findButton(dialog, "Aceptar").doClick();
            });

            withDialog(item::doClick, dialog -> {
                PageSetupPanel reopened = findComponent(dialog, PageSetupPanel.class);
                assertEquals(
                        150, reopened.toPageSetup().scorePercent(),
                        "el tamano de partitura elegido en el dialogo real tiene que seguir aplicado al reabrirlo");

                findButton(dialog, "Cancelar").doClick();
            });
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
