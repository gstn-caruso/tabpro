package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithMeasures;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.pressKey;
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
class SelectionPaintingAuditTest {

    @Test
    void ctrlAPaintsTheSelectionOfTheWholeScoreOnTheRealCanvas() throws Exception {
        Editor editor = editorWithMeasures(2);
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);

            pressKey(canvas, KeyStroke.getKeyStroke("ctrl A"));

            assertTrue(canvas.selection().isPresent(),
                    "Ctrl+A, really dispatched on the canvas, must leave something to paint");
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void anArrowWithoutShiftClearsTheSelectionCtrlALeft() throws Exception {
        Editor editor = editorWithMeasures(2);
        MainFrame frame = newFrame(editor);
        try {
            ScoreCanvas canvas = findComponent(frame.getContentPane(), ScoreCanvas.class);
            pressKey(canvas, KeyStroke.getKeyStroke("ctrl A"));

            pressKey(canvas, KeyStroke.getKeyStroke("RIGHT"));

            assertTrue(canvas.selection().isEmpty(),
                    "an arrow without Shift, really dispatched, must clear what Ctrl+A left");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
