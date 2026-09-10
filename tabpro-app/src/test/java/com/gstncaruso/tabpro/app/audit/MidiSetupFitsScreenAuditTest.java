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
import javax.swing.UIManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class MidiSetupFitsScreenAuditTest {

    @Test
    void midiSetupFitsOnScreenWithAcceptAndCancelAlwaysVisible() throws Exception {
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
     * The Look and Feel resolves "OptionPane.okButtonText"/"OptionPane.cancelButtonText" from the
     * JVM locale, and the CI runner has no Spanish locale installed: this test reproduces that
     * without depending on the environment, by overriding those two UIManager keys.
     */
    @Test
    void midiSetupShowsAcceptAndCancelEvenIfTheLookAndFeelTranslatesThemToAnotherLanguage() throws Exception {
        Object originalOk = UIManager.get("OptionPane.okButtonText");
        Object originalCancel = UIManager.get("OptionPane.cancelButtonText");
        UIManager.getDefaults().put("OptionPane.okButtonText", "OK");
        UIManager.getDefaults().put("OptionPane.cancelButtonText", "Cancel");

        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor, devicesWithAValidSensitivity());
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Configuración MIDI…");
            assertNotNull(item, "no encontre 'Configuración MIDI…' en el menu real");

            withDialog(item::doClick, dialog -> {
                assertNotNull(findButton(dialog, "Aceptar"),
                        "el dialogo real tiene que decir 'Aceptar' aunque el Look and Feel "
                                + "resuelva sus textos a otro idioma");
                assertNotNull(findButton(dialog, "Cancelar"),
                        "el dialogo real tiene que decir 'Cancelar' aunque el Look and Feel "
                                + "resuelva sus textos a otro idioma");
                findButton(dialog, "Cancelar").doClick();
            });
        } finally {
            AuditSupport.dispose(frame);
            UIManager.getDefaults().put("OptionPane.okButtonText", originalOk);
            UIManager.getDefaults().put("OptionPane.cancelButtonText", originalCancel);
        }
    }

    private static Ports.Devices devicesWithAValidSensitivity() {
        return (Ports.Devices) Proxy.newProxyInstance(
                Ports.Devices.class.getClassLoader(),
                new Class<?>[] {Ports.Devices.class},
                (proxy, method, args) -> "sensitivityMillis".equals(method.getName())
                        ? 60
                        : method.invoke(Ports.Devices.NONE, args));
    }

    /** isShowing() is not enough: inside a clipped scroll pane, the button still has a peer. */
    private static void assertWithinTheDialog(JDialog dialog, JButton button, String label) {
        assertNotNull(button, "no encontre el boton real '" + label + "'");
        Rectangle onScreen = new Rectangle(button.getLocationOnScreen(), button.getSize());
        Rectangle dialogOnScreen = new Rectangle(dialog.getLocationOnScreen(), dialog.getSize());
        assertTrue(dialogOnScreen.contains(onScreen),
                label + " (" + onScreen + ") tiene que quedar dentro del dialogo real (" + dialogOnScreen
                        + "), no tapado por un scroll");
    }
}
