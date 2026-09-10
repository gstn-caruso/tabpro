package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.dispatchKeyAndDetectDialog;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithANote;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButton;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findCheckBox;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.effects.SoundParameter;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class InsertParameterChangesAuditTest {

    @Test
    void elMenuCambioDeParametrosAbreElDialogoRealYElVolumenElegidoLlegaAlModelo() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Cambio de parámetros…");
            assertNotNull(item, "no encontre 'Cambio de parámetros…' en el menu real");
            assertEquals(KeyStroke.getKeyStroke("F10"), item.getAccelerator());

            withDialog(item::doClick, dialog -> {
                JCheckBox volumen = findCheckBox(dialog, "Volumen");
                assertNotNull(volumen, "no encontre la casilla real de Volumen");
                if (!volumen.isSelected()) {
                    volumen.doClick();
                }
                JSpinner spinner = findComponent(volumen.getParent(), JSpinner.class);
                assertNotNull(spinner, "no encontre el spinner real de Volumen");
                spinner.setValue(50);

                findButton(dialog, "Aceptar").doClick();
            });

            var change = editor.currentBeat().effects().parameterChange();
            assertTrue(change.changes(SoundParameter.VOLUME),
                    "la casilla real tildada tiene que quedar marcada en el modelo");
            assertEquals(50, change.valueOf(SoundParameter.VOLUME).orElseThrow(),
                    "el valor elegido en el spinner real tiene que ser el que quedo en el modelo");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    /**
     * Swing binds F10 without a modifier, out of the box (BasicMenuBarUI), to activate the
     * JMenuBar itself for arrow-key navigation. AcceleratorGuard neutralizes that key on the real
     * menu bar, so the app's own shortcut wins: it opens the real Change Parameters dialog
     * instead of activating the menu bar.
     */
    @Test
    void f10ConLaPartituraEnfocadaAbreElCambioDeParametrosEnVezDeActivarElMenu() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            KeyStroke f10 = KeyStroke.getKeyStroke("F10");
            int seleccionAntes = frame.getJMenuBar().getSelectionModel().getSelectedIndex();

            boolean abrioUnDialogo = dispatchKeyAndDetectDialog(canvas, f10, 800);

            int seleccionDespues = frame.getJMenuBar().getSelectionModel().getSelectedIndex();
            assertEquals(-1, seleccionAntes);
            assertTrue(abrioUnDialogo,
                    "F10 con la partitura enfocada tiene que abrir 'Cambio de parámetros'");
            assertEquals(-1, seleccionDespues,
                    "F10 no tiene que activar la barra de menus para navegarla con las flechas");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
