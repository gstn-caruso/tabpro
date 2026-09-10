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

@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class ConfigureTheSoundAuditTest {

    @Test
    void f2ByTheShortcutTurnsOnTheRealSoundBankInDevices() throws Exception {
        Editor editor = blankEditor();
        AuditSupport.RecordingDevices devices = new AuditSupport.RecordingDevices();
        MainFrame frame = AuditSupport.newFrame(editor, devices);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Banco de sonido");
            assertNotNull(item, "no encontre 'Banco de sonido' en el menu real");
            assertEquals(KeyStroke.getKeyStroke("F2"), item.getAccelerator());

            boolean before = devices.soundFontActive();
            int togglesBefore = devices.toggleCount();

            pressKey(canvas, KeyStroke.getKeyStroke("F2"));

            assertEquals(togglesBefore + 1, devices.toggleCount(),
                    "F2, despachado de verdad sobre el lienzo, tiene que llegar al Devices real");
            assertEquals(!before, devices.soundFontActive(), "F2 tiene que alternar el banco de sonido real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void f2WithTheScoreFocusedSynchronizesTheRealToolbarButton() throws Exception {
        Editor editor = blankEditor();
        AuditSupport.RecordingDevices devices = new AuditSupport.RecordingDevices();
        MainFrame frame = AuditSupport.newFrame(editor, devices);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            JToggleButton button = AuditSupport.findToggleButtonByActionName(frame.getContentPane(), "Banco de sonido");
            assertNotNull(button, "no encontre el boton conmutable real de 'Banco de sonido'");
            assertEquals(devices.soundFontActive(), button.isSelected(),
                    "el boton tiene que arrancar mostrando el estado real del banco de sonido");
            boolean before = button.isSelected();

            pressKey(canvas, KeyStroke.getKeyStroke("F2"));

            assertEquals(!before, button.isSelected(), "F2 tiene que sincronizar el boton conmutable real");
            assertEquals(devices.soundFontActive(), button.isSelected());
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void theMenuOffersTheMetronomeConfigurationAndTheChosenVolumeReachesTheTransport() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = AuditSupport.newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Configuración del metrónomo…");
            assertNotNull(item, "no encontre 'Configuración del metrónomo…' en el menu real");

            withDialog(item::doClick, dialog -> {
                JSlider volume = findComponent(dialog, JSlider.class);
                assertNotNull(volume, "no encontre el slider real de Volumen");
                volume.setValue(42);

                findButton(dialog, "Aceptar").doClick();
            });

            assertEquals(42, frame.transport().metronomeVolume(),
                    "el volumen elegido en el slider real tiene que llegar al Transport real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
