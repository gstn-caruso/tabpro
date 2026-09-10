package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.pressKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import com.gstncaruso.tabpro.ui.score.ScoreCanvas;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class CutCopyPasteAuditTest {

    @Test
    void copyingWithCtrlCFillsTheEditorsRealClipboard() throws Exception {
        Editor editor = blankEditor();
        editor.setFret(5);
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            assertTrue(editor.clipboard().content().isEmpty(), "el portapapeles arranca vacio");

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl C"));

            assertFalse(editor.clipboard().content().isEmpty(),
                    "Ctrl+C, despachado de verdad sobre el lienzo, tiene que llenar el portapapeles real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void copyingAndPastingWithTheRealShortcutsWriteTheSameNoteInAnotherBar() throws Exception {
        Editor editor = blankEditor();
        editor.insertMeasure();
        editor.moveToLastMeasure();
        editor.setFret(7);
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl C"));

            editor.moveToFirstMeasure();
            assertTrue(editor.currentNote().isEmpty(), "el primer compas arranca en silencio");

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl V"));

            assertEquals(7, editor.currentNote().orElseThrow().fret(),
                    "Ctrl+V, despachado de verdad, tiene que escribir la nota copiada en el compas de destino");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
