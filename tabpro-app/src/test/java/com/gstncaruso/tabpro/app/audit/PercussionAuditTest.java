package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.ui.MainFrame;
import javax.swing.JMenuItem;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class PercussionAuditTest {

    @Test
    void thePercussionWizardOpensARealDialogOnAPercussionTrack() throws Exception {
        Editor editor = blankEditor();
        editor.addTrack(Track.percussion("Batería"));
        editor.selectTrack(1);
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Asistente de percusión");
            assertNotNull(item, "could not find 'Asistente de percusión' in the real menu");

            withDialog(item::doClick, dialog -> dialog.dispose());
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void thePercussionWizardWarnsInsteadOfOpeningOnATrackThatIsNotPercussion() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Asistente de percusión");
            assertNotNull(item, "could not find 'Asistente de percusión' in the real menu");

            withDialog(item::doClick, dialog -> dialog.dispose());
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
