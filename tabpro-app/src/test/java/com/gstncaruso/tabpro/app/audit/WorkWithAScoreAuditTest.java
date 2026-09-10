package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButton;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Manual, "Work with a Score" (linea 1723 del texto extraido): agregar una pista (Pista >
 * Agregar, Ctrl+Shift+Insert) y sus propiedades (F6, ya documentado como atajo mudo en
 * KeyboardShortcutsAuditTest; aca se ejercita el menu, que si funciona). Ambos dialogos reales,
 * detectados por WINDOW_OPENED, con sus controles reales (campo de texto, radio buttons).
 */
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class WorkWithAScoreAuditTest {

    @Test
    void agregarUnaPistaPorElAtajoCtrlShiftInsertCreaUnaPistaDePercusionConElNombreElegido() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Agregar una pista…");
            assertNotNull(item, "no encontre 'Agregar una pista…' en el menu real");
            assertEquals(KeyStroke.getKeyStroke("ctrl shift INSERT"), item.getAccelerator());

            int pistasAntes = editor.score().trackCount();

            withDialog(() -> canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(), KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK,
                    KeyEvent.VK_INSERT, KeyEvent.CHAR_UNDEFINED)), dialog -> {
                JTextField nombre = findComponent(dialog, JTextField.class);
                assertNotNull(nombre, "no encontre el campo de texto real del nombre");
                nombre.setText("Batería nueva");

                JRadioButton percusion = AuditSupport.findRadioButton(dialog, "Percusión");
                assertNotNull(percusion, "no encontre el radio button real de Percusión");
                percusion.doClick();

                findButton(dialog, "Aceptar").doClick();
            });

            assertEquals(pistasAntes + 1, editor.score().trackCount(),
                    "el atajo real tiene que agregar una pista al modelo");
            var nueva = editor.score().track(editor.score().trackCount() - 1);
            assertEquals("Batería nueva", nueva.name(),
                    "el nombre tecleado en el campo real tiene que ser el de la pista nueva");
            assertTrue(nueva.settings().percussion(),
                    "el radio button real de Percusión tiene que dejar la pista como percusion");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void propiedadesDeLaPistaPorElMenuRenombraLaPistaReal() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Propiedades de la pista…");
            assertNotNull(item, "no encontre 'Propiedades de la pista…' en el menu real");

            withDialog(item::doClick, dialog -> {
                JTextField nombre = findComponent(dialog, JTextField.class);
                assertNotNull(nombre, "no encontre el campo de texto real del nombre de la pista");
                nombre.setText("Guitarra renombrada");

                findButton(dialog, "Aceptar").doClick();
            });

            assertEquals("Guitarra renombrada", editor.score().track(0).name(),
                    "el nombre tecleado en el campo real del dialogo tiene que quedar en el modelo");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
