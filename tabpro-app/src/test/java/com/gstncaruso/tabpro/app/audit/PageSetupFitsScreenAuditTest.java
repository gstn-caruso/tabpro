package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButton;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Manual, "Page Setup": el dialogo real de Configurar pagina (F8) nunca supera el area util de
 * la pantalla, y sus tres botones extra -Actualizar partitura, Guardar como configuracion por
 * defecto, Aplicar configuracion por defecto- quedan visibles fuera de cualquier scroll, igual
 * que Aceptar y Cancelar.
 */
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class PageSetupFitsScreenAuditTest {

    @Test
    void configurarPaginaEntraEnPantallaConLosTresBotonesDelManualSiempreVisibles() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Configurar página…");
            assertNotNull(item, "no encontre 'Configurar página…' en el menu real");

            withDialog(item::doClick, dialog -> {
                Dimension screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize();
                assertTrue(dialog.getHeight() <= screen.height,
                        "el dialogo real (" + dialog.getHeight() + "px) no puede ser mas alto que la pantalla ("
                                + screen.height + "px)");

                assertWithinTheDialog(dialog, findButton(dialog, "Actualizar partitura"),
                        "Actualizar partitura");
                assertWithinTheDialog(dialog, findButton(dialog, "Guardar como configuracion por defecto"),
                        "Guardar como configuracion por defecto");
                assertWithinTheDialog(dialog, findButton(dialog, "Aplicar configuracion por defecto"),
                        "Aplicar configuracion por defecto");
                assertWithinTheDialog(dialog, findButton(dialog, "Aceptar"), "Aceptar");
                assertWithinTheDialog(dialog, findButton(dialog, "Cancelar"), "Cancelar");

                findButton(dialog, "Cancelar").doClick();
            });
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    /** No alcanza con "showing": adentro de un scroll clippeado, el boton sigue teniendo peer. */
    private static void assertWithinTheDialog(JDialog dialog, JButton button, String label) {
        assertNotNull(button, "no encontre el boton real '" + label + "'");
        Rectangle onScreen = new Rectangle(button.getLocationOnScreen(), button.getSize());
        Rectangle dialogOnScreen = new Rectangle(dialog.getLocationOnScreen(), dialog.getSize());
        assertTrue(dialogOnScreen.contains(onScreen),
                label + " (" + onScreen + ") tiene que quedar dentro del dialogo real (" + dialogOnScreen
                        + "), no tapado por un scroll");
    }
}
