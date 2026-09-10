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
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
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
 * Manual, "Print a Score" (linea 2207 del texto extraido): "Page Setup" (F8, papel, orientacion,
 * margenes, tamano de partitura) y "Print" (Ctrl+P, toda la partitura o un rango de paginas,
 * escala fija o Ajustar a la hoja).
 *
 * <p>El {@code PrinterJob} real de AWT no tiene costura: {@link ScorePrinting#print} y
 * {@link ScorePrinting#configurePrinterPage} llaman a {@code PrinterJob.getPrinterJob()}
 * directamente, y esa llamada abre el dialogo <em>nativo</em> del sistema operativo -no un
 * {@code JDialog} de Swing-, que ademas depende de que haya algun servicio de impresion instalado.
 * Por eso esta clase nunca aprieta "Imprimir" ni "Configurar…" en el dialogo real: verifica todo
 * el camino hasta ahi (que se abre con los datos reales de la partitura, que sus controles reales
 * reflejan lo que se elige) y despues cierra con Cancelar, que es la unica forma de probar el
 * camino sin arriesgar que el proceso quede esperando una ventana nativa que nadie va a atender.
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

    /**
     * Con un {@link com.gstncaruso.tabpro.ui.print.Printing} falso inyectado en el MainFrame de
     * prueba, apretar "Imprimir" de verdad -no Cancelar- es seguro: nunca se llega a un
     * {@code PrinterJob} real. Prueba que el rango elegido en el dialogo real de tabpro llega tal
     * cual al {@code Printable} que recibe el PrinterJob (falso), y que ese Printable pinta la
     * partitura real y no una en blanco.
     */
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

    /** Un papel bien grande, para que ninguna hoja real quede recortada por el tamano del papel. */
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

    /**
     * Manual, "Page Setup": papel, orientacion, margenes y tamano de partitura, con los tres
     * botones extra (Actualizar partitura, Guardar como configuracion por defecto, Aplicar
     * configuracion por defecto). Se ejercita solo el camino que el manual describe primero: F5
     * [sic, F8] abre el dialogo real, cambiar el tamano de partitura y Aceptar deja la eleccion
     * aplicada para la proxima vez que se abre la misma ventana.
     */
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
