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
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(DialogShellFitsScreenTest.SWING_LOCK)
class DialogShellFitsScreenTest {

    static final String SWING_LOCK = "tabpro-audit-swing";

    @Test
    void contentTallerThanTheScreenDoesNotMakeTheDialogExceedItAndAcceptStaysVisible() throws Exception {
        JFrame owner = new JFrame();
        JPanel anchor = anchoredTo(owner);
        JPanel tallContent = new JPanel();
        tallContent.setPreferredSize(new Dimension(300, 5000));

        JDialog dialog = openDialogAndWait(owner, () -> DialogShell.ask(anchor, "Prueba", tallContent));
        try {
            assertFitsTheScreen(dialog);
            JButton accept = findButton(dialog, "Aceptar");
            assertNotNull(accept, "could not find the real Aceptar button inside the dialog");
            assertTrue(accept.isShowing(), "Aceptar must stay visible outside of any scroll");
        } finally {
            SwingUtilities.invokeAndWait(dialog::dispose);
            owner.dispose();
        }
    }

    @Test
    void contentTallerThanTheScreenDoesNotMakeACloseOnlyWindowExceedItAndCloseStaysVisible() throws Exception {
        JFrame owner = new JFrame();
        JPanel anchor = anchoredTo(owner);
        JPanel tallContent = new JPanel();
        tallContent.setPreferredSize(new Dimension(300, 5000));

        JDialog dialog = openDialogAndWait(owner, () -> DialogShell.show(anchor, "Prueba", tallContent));
        try {
            assertFitsTheScreen(dialog);
            JButton close = findButton(dialog, "Cerrar");
            assertNotNull(close, "could not find the real Cerrar button inside the dialog");
            assertTrue(close.isShowing(), "Cerrar must stay visible outside of any scroll");
        } finally {
            SwingUtilities.invokeAndWait(dialog::dispose);
            owner.dispose();
        }
    }

    @Test
    void extraButtonsStayVisibleOutsideTheScrollNextToTheAcceptAndCancelBar() throws Exception {
        JFrame owner = new JFrame();
        JPanel anchor = anchoredTo(owner);
        JPanel tallContent = new JPanel();
        tallContent.setPreferredSize(new Dimension(300, 5000));
        JButton extra = new JButton("Actualizar partitura");
        JPanel extraButtons = new JPanel();
        extraButtons.add(extra);

        JDialog dialog = openDialogAndWait(
                owner, () -> DialogShell.ask(anchor, "Prueba", tallContent, extraButtons, "Aceptar", null));
        try {
            assertFitsTheScreen(dialog);
            assertTrue(extra.isShowing(), "the extra button must stay visible outside of any scroll");
            assertTrue(findButton(dialog, "Aceptar").isShowing());
        } finally {
            SwingUtilities.invokeAndWait(dialog::dispose);
            owner.dispose();
        }
    }

    private static JPanel anchoredTo(JFrame owner) {
        JPanel anchor = new JPanel();
        owner.add(anchor);
        return anchor;
    }

    private static JDialog openDialogAndWait(JFrame owner, Runnable trigger) throws Exception {
        JDialog[] captured = new JDialog[1];
        CountDownLatch opened = new CountDownLatch(1);
        AWTEventListener listener = event -> {
            if (event.getID() == WindowEvent.WINDOW_OPENED
                    && event.getSource() instanceof JDialog dialog
                    && dialog.getOwner() == owner) {
                captured[0] = dialog;
                opened.countDown();
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.WINDOW_EVENT_MASK);
        try {
            new Thread(trigger).start();
            assertTrue(opened.await(5, TimeUnit.SECONDS), "the real dialog never opened");
            return captured[0];
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
        }
    }

    private static void assertFitsTheScreen(JDialog dialog) {
        Dimension screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize();
        assertTrue(dialog.getHeight() <= screen.height,
                "the real dialog (" + dialog.getHeight() + "px) cannot be taller than the screen (" + screen.height + "px)");
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
