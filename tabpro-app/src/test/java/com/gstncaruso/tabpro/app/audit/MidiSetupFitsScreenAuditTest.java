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
import com.gstncaruso.tabpro.ui.actions.Ports;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.lang.reflect.Proxy;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Manual, "MIDI Setup": el dialogo real de Configuracion MIDI nunca supera el area util de la
 * pantalla, con Aceptar y Cancelar siempre visibles fuera de cualquier scroll.
 */
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class MidiSetupFitsScreenAuditTest {

    @Test
    void configuracionMidiEntraEnPantallaConAceptarYCancelarSiempreVisibles() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor, devicesWithAValidSensitivity());
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Configuración MIDI…");
            assertNotNull(item, "no encontre 'Configuración MIDI…' en el menu real");

            withDialog(item::doClick, dialog -> {
                Dimension screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize();
                assertTrue(dialog.getHeight() <= screen.height,
                        "el dialogo real (" + dialog.getHeight() + "px) no puede ser mas alto que la pantalla ("
                                + screen.height + "px)");

                assertWithinTheDialog(dialog, findButton(dialog, "Aceptar"), "Aceptar");
                assertWithinTheDialog(dialog, findButton(dialog, "Cancelar"), "Cancelar");

                findButton(dialog, "Cancelar").doClick();
            });
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    /**
     * {@code Ports.Devices.NONE} da {@code sensitivityMillis()=0}, fuera del rango 1-2000 del
     * spinner real (ver auditoria visual): un proxy que delega todo salvo ese valor.
     */
    private static Ports.Devices devicesWithAValidSensitivity() {
        return (Ports.Devices) Proxy.newProxyInstance(
                Ports.Devices.class.getClassLoader(),
                new Class<?>[] {Ports.Devices.class},
                (proxy, method, args) -> "sensitivityMillis".equals(method.getName())
                        ? 60
                        : method.invoke(Ports.Devices.NONE, args));
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
