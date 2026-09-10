package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.pressKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Manual, "Configure the Sound" (linea 1945 del texto extraido): F2 activa o desactiva el banco
 * de sonido cargado (Ports.Devices real, no el Devices.NONE de los demas capitulos, para poder
 * mirar desde afuera si el atajo real lo prendio).
 *
 * <p>Nota aparte, encontrada revisando el codigo mientras se armaba este capitulo (no hace falta
 * un test dinamico: es una ausencia, no una mentira): {@code Ports.Dialogs.metronomeSettings()}
 * esta declarado, implementado en {@code MainFrame.Windows} y hasta tiene su propio
 * {@code MetronomeDialog}, pero ningun comando de {@code Commands.java} ni ningun JMenuItem de
 * {@code MenuBar.java} lo llama: el dialogo de volumen del metronomo del manual no tiene forma
 * de abrirse desde la interfaz real.
 */
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class ConfigureTheSoundAuditTest {

    @Test
    void f2PorElAtajoPrendeElBancoDeSonidoRealDeDevices() throws Exception {
        Editor editor = blankEditor();
        AuditSupport.RecordingDevices devices = new AuditSupport.RecordingDevices();
        MainFrame frame = AuditSupport.newFrame(editor, devices);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Banco de sonido");
            assertNotNull(item, "no encontre 'Banco de sonido' en el menu real");
            assertEquals(KeyStroke.getKeyStroke("F2"), item.getAccelerator());

            boolean antes = devices.soundFontActive();
            int cambiosAntes = devices.toggleCount();

            pressKey(canvas, KeyStroke.getKeyStroke("F2"));

            assertEquals(cambiosAntes + 1, devices.toggleCount(),
                    "F2, despachado de verdad sobre el lienzo, tiene que llegar al Devices real");
            assertEquals(!antes, devices.soundFontActive(), "F2 tiene que alternar el banco de sonido real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
