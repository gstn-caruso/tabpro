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
            assertNotNull(item, "could not find 'Agregar una pista…' in the real menu");
            assertEquals(KeyStroke.getKeyStroke("ctrl shift INSERT"), item.getAccelerator());

            int tracksBefore = editor.score().trackCount();

            withDialog(() -> canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(), KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK,
                    KeyEvent.VK_INSERT, KeyEvent.CHAR_UNDEFINED)), dialog -> {
                JTextField name = findComponent(dialog, JTextField.class);
                assertNotNull(name, "could not find the real name text field");
                name.setText("New Drums");

                JRadioButton percussion = AuditSupport.findRadioButton(dialog, "Percusión");
                assertNotNull(percussion, "could not find the real Percusión radio button");
                percussion.doClick();

                findButton(dialog, "Aceptar").doClick();
            });

            assertEquals(tracksBefore + 1, editor.score().trackCount(),
                    "the real shortcut must add a track to the model");
            var newTrack = editor.score().track(editor.score().trackCount() - 1);
            assertEquals("New Drums", newTrack.name(),
                    "what was typed in the real field must be the new track's name");
            assertTrue(newTrack.settings().percussion(),
                    "the real Percusión radio button must leave the track as percussion");
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
            assertNotNull(item, "could not find 'Propiedades de la pista…' in the real menu");

            withDialog(item::doClick, dialog -> {
                JTextField name = findComponent(dialog, JTextField.class);
                assertNotNull(name, "could not find the real track name text field");
                name.setText("Renamed Guitar");

                findButton(dialog, "Aceptar").doClick();
            });

            assertEquals("Renamed Guitar", editor.score().track(0).name(),
                    "what was typed in the real dialog's field must end up in the model");
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
                    "a new track does not show the string names by default");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Propiedades de la pista…");
            assertNotNull(item, "could not find 'Propiedades de la pista…' in the real menu");

            withDialog(item::doClick, dialog -> {
                JCheckBox tuning = AuditSupport.findCheckBox(dialog, "Afinación");
                assertNotNull(tuning, "could not find the real 'Afinación' checkbox");
                tuning.doClick();

                findButton(dialog, "Aceptar").doClick();
            });

            assertTrue(editor.score().track(0).settings().display().tuningLegend(),
                    "checking 'Afinación' in the real dialog must turn on the real model's legend");
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
                    "a new track does not force channels 11 to 16 by default");

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Propiedades de la pista…");
            assertNotNull(item, "could not find 'Propiedades de la pista…' in the real menu");

            withDialog(item::doClick, dialog -> {
                JCheckBox forceChannels = AuditSupport.findCheckBox(dialog, "Forzar canales 11 a 16");
                assertNotNull(forceChannels, "could not find the real 'Forzar canales 11 a 16' checkbox");
                forceChannels.doClick();

                findButton(dialog, "Aceptar").doClick();
            });

            assertTrue(editor.score().track(0).settings().forceChannels11to16(),
                    "checking 'Forzar canales 11 a 16' in the real dialog must turn it on in the real model");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
