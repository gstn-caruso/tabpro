package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class TrackPropertiesForceHorizontalBeamsAuditTest {

    @Test
    void theCheckboxInTheRealDialogFlattensTheRealBeamThroughF6() throws Exception {
        Editor editor = ascendingBeamEditor();
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            assertFalse(editor.score().track(0).settings().display().forceHorizontalBeams(),
                    "starts without forcing horizontal beams");
            BufferedImage before = renderingOf(canvas);

            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Propiedades de la pista…");
            assertNotNull(item, "could not find 'Propiedades de la pista…' in the real menu");
            assertEquals(KeyStroke.getKeyStroke("F6"), item.getAccelerator());

            withDialog(item::doClick, dialog -> {
                JCheckBox checkbox = AuditSupport.findCheckBox(dialog, "Forzar barras horizontales");
                assertNotNull(checkbox, "could not find the real 'Forzar barras horizontales' checkbox");
                assertFalse(checkbox.isSelected(), "the checkbox starts unchecked");

                checkbox.setSelected(true);
                AuditSupport.findButton(dialog, "Aceptar").doClick();
            });

            assertTrue(editor.score().track(0).settings().display().forceHorizontalBeams(),
                    "the real checkbox, checked and accepted, must reach the track's real model");

            BufferedImage after = renderingOf(canvas);
            assertFalse(imagesLookTheSame(before, after),
                    "with the beam now forced to horizontal, the real staff must render differently");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    private static Editor ascendingBeamEditor() {
        Editor editor = blankEditor();
        editor.moveTo(0, 0, 3);
        editor.setNoteValue(NoteValue.EIGHTH);
        editor.setFret(0);
        editor.moveRight();
        editor.setFret(2);
        return editor;
    }

    private static BufferedImage renderingOf(Container root) {
        BufferedImage image = new BufferedImage(root.getWidth(), root.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D canvas = image.createGraphics();
        root.printAll(canvas);
        canvas.dispose();
        return image;
    }

    private static boolean imagesLookTheSame(BufferedImage first, BufferedImage second) {
        if (first.getWidth() != second.getWidth() || first.getHeight() != second.getHeight()) {
            return false;
        }
        for (int x = 0; x < first.getWidth(); x++) {
            for (int y = 0; y < first.getHeight(); y++) {
                if (first.getRGB(x, y) != second.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
}
