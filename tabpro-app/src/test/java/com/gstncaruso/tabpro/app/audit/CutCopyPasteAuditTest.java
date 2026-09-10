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
            assertTrue(editor.clipboard().content().isEmpty(), "the clipboard starts empty");

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl C"));

            assertFalse(editor.clipboard().content().isEmpty(),
                    "Ctrl+C, really dispatched on the canvas, must fill the real clipboard");
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
            assertTrue(editor.currentNote().isEmpty(), "the first bar starts silent");

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl V"));

            assertEquals(7, editor.currentNote().orElseThrow().fret(),
                    "Ctrl+V, really dispatched, must write the copied note in the destination bar");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
