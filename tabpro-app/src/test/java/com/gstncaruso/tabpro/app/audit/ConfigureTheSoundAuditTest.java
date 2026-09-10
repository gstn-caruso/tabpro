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

@Tag("integration")
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
            assertNotNull(item, "could not find 'Banco de sonido' in the real menu");
            assertEquals(KeyStroke.getKeyStroke("F2"), item.getAccelerator());

            boolean before = devices.soundFontActive();
            int togglesBefore = devices.toggleCount();

            pressKey(canvas, KeyStroke.getKeyStroke("F2"));

            assertEquals(togglesBefore + 1, devices.toggleCount(),
                    "F2, really dispatched on the canvas, must reach the real Devices");
            assertEquals(!before, devices.soundFontActive(), "F2 must toggle the real sound bank");
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
            assertNotNull(button, "could not find the real toggle button for 'Banco de sonido'");
            assertEquals(devices.soundFontActive(), button.isSelected(),
                    "the button must start showing the real sound bank state");
            boolean before = button.isSelected();

            pressKey(canvas, KeyStroke.getKeyStroke("F2"));

            assertEquals(!before, button.isSelected(), "F2 must sync the real toggle button");
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
            assertNotNull(item, "could not find 'Configuración del metrónomo…' in the real menu");

            withDialog(item::doClick, dialog -> {
                JSlider volume = findComponent(dialog, JSlider.class);
                assertNotNull(volume, "could not find the real Volumen slider");
                volume.setValue(42);

                findButton(dialog, "Aceptar").doClick();
            });

            assertEquals(42, frame.transport().metronomeVolume(),
                    "the volume chosen in the real slider must reach the real Transport");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
