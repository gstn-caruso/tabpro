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

/**
 * Manual, "Insert Parameter Changes" (linea 1340 del texto extraido): el cambio de parametros
 * (Nota > Mesa de mezcla, F10) tilda que parametro cambia (volumen, paneo, tempo, ...), le pone
 * un valor y una transicion. El dialogo se ejercita por el menu real -detectado por
 * WINDOW_OPENED-, se tilda la casilla real de Volumen, se cambia el spinner real y se lee lo que
 * quedo en el modelo despues de aceptar. El atajo F10 se ejercita aparte porque, como se ve mas
 * abajo, ni siquiera llega a abrir el dialogo.
 */
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
     * F10 sin modificador es, de fabrica en Swing (BasicMenuBarUI), la tecla que activa el
     * propio JMenuBar para navegarlo con las flechas. AcceleratorGuard neutraliza esa tecla en
     * la barra de menus real, asi que el atajo documentado en el manual gana: abre el dialogo
     * real de Cambio de parametros, sin activar la barra de menus.
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
