package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButton;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import java.awt.event.KeyEvent;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class WorkWithAScoreAuditTest {

    @Test
    void addingATrackByTheCtrlShiftInsertShortcutCreatesAPercussionTrackWithTheChosenName() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Agregar una pista…");
            assertNotNull(item, "no encontre 'Agregar una pista…' en el menu real");
            assertEquals(KeyStroke.getKeyStroke("ctrl shift INSERT"), item.getAccelerator());

            int tracksBefore = editor.score().trackCount();

            withDialog(() -> canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(), KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK,
                    KeyEvent.VK_INSERT, KeyEvent.CHAR_UNDEFINED)), dialog -> {
                JTextField name = findComponent(dialog, JTextField.class);
                assertNotNull(name, "no encontre el campo de texto real del nombre");
                name.setText("Batería nueva");

                JRadioButton percussion = AuditSupport.findRadioButton(dialog, "Percusión");
                assertNotNull(percussion, "no encontre el radio button real de Percusión");
                percussion.doClick();

                findButton(dialog, "Aceptar").doClick();
            });

            assertEquals(tracksBefore + 1, editor.score().trackCount(),
                    "el atajo real tiene que agregar una pista al modelo");
            var newTrack = editor.score().track(editor.score().trackCount() - 1);
            assertEquals("Batería nueva", newTrack.name(),
                    "el nombre tecleado en el campo real tiene que ser el de la pista nueva");
            assertTrue(newTrack.settings().percussion(),
                    "el radio button real de Percusión tiene que dejar la pista como percusion");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void trackPropertiesThroughTheMenuRenamesTheRealTrack() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Propiedades de la pista…");
            assertNotNull(item, "no encontre 'Propiedades de la pista…' en el menu real");

            withDialog(item::doClick, dialog -> {
                JTextField name = findComponent(dialog, JTextField.class);
                assertNotNull(name, "no encontre el campo de texto real del nombre de la pista");
                name.setText("Guitarra renombrada");

                findButton(dialog, "Aceptar").doClick();
            });

            assertEquals("Guitarra renombrada", editor.score().track(0).name(),
                    "el nombre tecleado en el campo real del dialogo tiene que quedar en el modelo");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void tuningInTrackPropertiesShowsTheStringNamesInTheRealModel() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            assertFalse(editor.score().track(0).settings().display().tuningLegend(),
                    "una pista nueva no muestra los nombres de cuerda por defecto");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Propiedades de la pista…");
            assertNotNull(item, "no encontre 'Propiedades de la pista…' en el menu real");

            withDialog(item::doClick, dialog -> {
                JCheckBox tuning = AuditSupport.findCheckBox(dialog, "Afinación");
                assertNotNull(tuning, "no encontre la casilla real 'Afinación'");
                tuning.doClick();

                findButton(dialog, "Aceptar").doClick();
            });

            assertTrue(editor.score().track(0).settings().display().tuningLegend(),
                    "tildar 'Afinación' en el dialogo real tiene que prender la leyenda en el modelo real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void forcingChannels11To16InTrackPropertiesIsSavedInTheRealModel() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            assertFalse(editor.score().track(0).settings().forceChannels11to16(),
                    "una pista nueva no fuerza los canales 11 a 16 por defecto");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Propiedades de la pista…");
            assertNotNull(item, "no encontre 'Propiedades de la pista…' en el menu real");

            withDialog(item::doClick, dialog -> {
                JCheckBox forceChannels = AuditSupport.findCheckBox(dialog, "Forzar canales 11 a 16");
                assertNotNull(forceChannels, "no encontre la casilla real 'Forzar canales 11 a 16'");
                forceChannels.doClick();

                findButton(dialog, "Aceptar").doClick();
            });

            assertTrue(editor.score().track(0).settings().forceChannels11to16(),
                    "tildar 'Forzar canales 11 a 16' en el dialogo real tiene que prenderlo en el modelo real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
