package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButton;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.pressKey;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Manual, "Configure the Sound" (linea 1945 del texto extraido): F2 activa o desactiva el banco
 * de sonido cargado (Ports.Devices real, no el Devices.NONE de los demas capitulos, para poder
 * mirar desde afuera si el atajo real lo prendio), y el menu Sonido ofrece la configuracion del
 * metronomo (volumen y actividad), que se ejercita por el dialogo real.
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

    /**
     * El test de arriba ya prueba que F2 llega al Devices real por el menu; este prueba que el
     * conmutable de la barra -otro control, el mismo comando- se entera del cambio y lo muestra,
     * no solo el item de menu que lo disparo. Una interfaz que no sincroniza sus controles entre
     * si es la "interfaz que miente" que esta auditoria persigue.
     */
    @Test
    void f2ConLaPartituraEnfocadaSincronizaElBotonRealDeLaBarra() throws Exception {
        Editor editor = blankEditor();
        AuditSupport.RecordingDevices devices = new AuditSupport.RecordingDevices();
        MainFrame frame = AuditSupport.newFrame(editor, devices);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            JToggleButton boton = AuditSupport.findToggleButtonByActionName(frame.getContentPane(), "Banco de sonido");
            assertNotNull(boton, "no encontre el boton conmutable real de 'Banco de sonido'");
            assertEquals(devices.soundFontActive(), boton.isSelected(),
                    "el boton tiene que arrancar mostrando el estado real del banco de sonido");
            boolean antes = boton.isSelected();

            pressKey(canvas, KeyStroke.getKeyStroke("F2"));

            assertEquals(!antes, boton.isSelected(), "F2 tiene que sincronizar el boton conmutable real");
            assertEquals(devices.soundFontActive(), boton.isSelected());
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    /**
     * Manual, "Configure the Sound" (linea 1945): el volumen del metronomo se elige en su propio
     * dialogo, abierto por el menu real y detectado por WINDOW_OPENED. El Transport no se puede
     * mirar desde afuera de MainFrame salvo por su propio metodo publico transport(): ahi vive el
     * volumen que el dialogo real, con su slider real, tiene que dejar despues de Aceptar.
     */
    @Test
    void elMenuOfreceLaConfiguracionDelMetronomoYElVolumenElegidoLlegaAlTransporte() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = AuditSupport.newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Configuración del metrónomo…");
            assertNotNull(item, "no encontre 'Configuración del metrónomo…' en el menu real");

            withDialog(item::doClick, dialog -> {
                JSlider volumen = findComponent(dialog, JSlider.class);
                assertNotNull(volumen, "no encontre el slider real de Volumen");
                volumen.setValue(42);

                findButton(dialog, "Aceptar").doClick();
            });

            assertEquals(42, frame.transport().metronomeVolume(),
                    "el volumen elegido en el slider real tiene que llegar al Transport real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
