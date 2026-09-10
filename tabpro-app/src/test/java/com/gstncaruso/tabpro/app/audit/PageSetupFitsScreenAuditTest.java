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

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class PageSetupFitsScreenAuditTest {

    @Test
    void pageSetupFitsOnScreenWithTheThreeManualButtonsAlwaysVisible() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Configurar página…");
            assertNotNull(item, "could not find 'Configurar página…' in the real menu");

            withDialog(item::doClick, dialog -> {
                Dimension screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize();
                assertTrue(dialog.getHeight() <= screen.height,
                        "the real dialog (" + dialog.getHeight() + "px) cannot be taller than the screen ("
                                + screen.height + "px)");

                assertWithinTheDialog(dialog, findButton(dialog, "Actualizar partitura"),
                        "Actualizar partitura");
                assertWithinTheDialog(dialog, findButton(dialog, "Guardar como configuración por defecto"),
                        "Guardar como configuración por defecto");
                assertWithinTheDialog(dialog, findButton(dialog, "Aplicar configuración por defecto"),
                        "Aplicar configuración por defecto");
                assertWithinTheDialog(dialog, findButton(dialog, "Aceptar"), "Aceptar");
                assertWithinTheDialog(dialog, findButton(dialog, "Cancelar"), "Cancelar");

                findButton(dialog, "Cancelar").doClick();
            });
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    /** isShowing() is not enough: inside a clipped scroll pane, the button still has a peer. */
    private static void assertWithinTheDialog(JDialog dialog, JButton button, String label) {
        assertNotNull(button, "could not find the real button '" + label + "'");
        Rectangle onScreen = new Rectangle(button.getLocationOnScreen(), button.getSize());
        Rectangle dialogOnScreen = new Rectangle(dialog.getLocationOnScreen(), dialog.getSize());
        assertTrue(dialogOnScreen.contains(onScreen),
                label + " (" + onScreen + ") must stay inside the real dialog (" + dialogOnScreen
                        + "), not covered by a scroll");
    }
}
