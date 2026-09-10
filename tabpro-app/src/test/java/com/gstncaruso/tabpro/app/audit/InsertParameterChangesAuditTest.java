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

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class InsertParameterChangesAuditTest {

    @Test
    void theChangeParametersMenuOpensTheRealDialogAndTheChosenVolumeReachesTheModel() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Cambio de parámetros…");
            assertNotNull(item, "could not find 'Cambio de parámetros…' in the real menu");
            assertEquals(KeyStroke.getKeyStroke("F10"), item.getAccelerator());

            withDialog(item::doClick, dialog -> {
                JCheckBox volume = findCheckBox(dialog, "Volumen");
                assertNotNull(volume, "could not find the real Volumen checkbox");
                if (!volume.isSelected()) {
                    volume.doClick();
                }
                JSpinner spinner = findComponent(volume.getParent(), JSpinner.class);
                assertNotNull(spinner, "could not find the real Volumen spinner");
                spinner.setValue(50);

                findButton(dialog, "Aceptar").doClick();
            });

            var change = editor.currentBeat().effects().parameterChange();
            assertTrue(change.changes(SoundParameter.VOLUME),
                    "the real checked checkbox must end up checked in the model");
            assertEquals(50, change.valueOf(SoundParameter.VOLUME).orElseThrow(),
                    "the value chosen in the real spinner must be the one that ended up in the model");
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
    void f10WithTheScoreFocusedOpensChangeParametersInsteadOfActivatingTheMenu() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            KeyStroke f10 = KeyStroke.getKeyStroke("F10");
            int selectionBefore = frame.getJMenuBar().getSelectionModel().getSelectedIndex();

            boolean openedADialog = dispatchKeyAndDetectDialog(canvas, f10, 800);

            int selectionAfter = frame.getJMenuBar().getSelectionModel().getSelectedIndex();
            assertEquals(-1, selectionBefore);
            assertTrue(openedADialog,
                    "F10 with the score focused must open 'Cambio de parámetros'");
            assertEquals(-1, selectionAfter,
                    "F10 must not activate the menu bar for arrow-key navigation");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
