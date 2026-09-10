package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithANote;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButton;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponent;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Score;
import com.gstncaruso.tabpro.ui.MainFrame;
import javax.swing.JMenuItem;
import javax.swing.JSpinner;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class WizardsAuditTest {

    @Test
    void transposingThroughTheMenuChangesTheRealScoreAccordingToTheSemitonesSpinner() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Transponer…");
            assertNotNull(item, "no encontre 'Transponer…' en el menu real");

            Score before = editor.score();

            withDialog(item::doClick, dialog -> {
                JSpinner semitones = findComponent(dialog, JSpinner.class);
                assertNotNull(semitones, "no encontre el spinner real de semitonos");
                semitones.setValue(2);

                findButton(dialog, "Transponer").doClick();
            });

            assertNotEquals(before, editor.score(),
                    "los 2 semitonos elegidos en el spinner real tienen que transponer la partitura real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
