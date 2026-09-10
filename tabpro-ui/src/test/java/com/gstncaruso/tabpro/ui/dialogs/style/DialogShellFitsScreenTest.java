package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * La regla general de {@link DialogShell} de punta a punta, con un dialogo real: necesita un
 * toolkit no headless (por eso "integracion", igual que la auditoria de uso real de tabpro-app).
 */
@Tag("integracion")
class DialogShellFitsScreenTest {

    @Test
    void unContenidoMasAltoQueLaPantallaNoHaceQueElDialogoLaSupereYAceptarSigueVisible() throws Exception {
        JFrame parent = new JFrame();
        JPanel tallContent = new JPanel();
        tallContent.setPreferredSize(new Dimension(300, 5000));

        JDialog[] captured = new JDialog[1];
        CountDownLatch opened = new CountDownLatch(1);
        AWTEventListener listener = event -> {
            if (event.getID() == WindowEvent.WINDOW_OPENED && event.getSource() instanceof JDialog dialog) {
                captured[0] = dialog;
                opened.countDown();
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.WINDOW_EVENT_MASK);
        Thread trigger = new Thread(() -> DialogShell.ask(parent, "Prueba", tallContent));
        trigger.start();
        try {
            assertTrue(opened.await(5, TimeUnit.SECONDS), "el dialogo real nunca abrio");
            JDialog dialog = captured[0];
            Dimension screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize();

            assertTrue(dialog.getHeight() <= screen.height,
                    "el dialogo real (" + dialog.getHeight() + "px) no puede ser mas alto que la pantalla (" + screen.height + "px)");
            JButton accept = findButton(dialog, "Aceptar");
            assertNotNull(accept, "no encontre el boton Aceptar real dentro del dialogo");
            assertTrue(accept.isShowing(), "Aceptar tiene que quedar visible fuera de cualquier scroll");

            SwingUtilities.invokeAndWait(findButton(dialog, "Cancelar")::doClick);
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
            trigger.join(5000);
            parent.dispose();
        }
    }

    private static JButton findButton(Container root, String text) {
        for (java.awt.Component component : root.getComponents()) {
            if (component instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container container) {
                JButton found = findButton(container, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
