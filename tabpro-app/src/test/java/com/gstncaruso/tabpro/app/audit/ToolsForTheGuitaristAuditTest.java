package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.blankEditor;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import javax.swing.JMenuItem;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class ToolsForTheGuitaristAuditTest {

    @Test
    void escalasPorElMenuAbreElDialogoReal() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Escalas…");
            assertNotNull(item, "no encontre 'Escalas…' en el menu real");

            withDialog(item::doClick, dialog -> dialog.dispose());
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void afinadorPorElMenuAbreElDialogoReal() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Afinador…");
            assertNotNull(item, "no encontre 'Afinador…' en el menu real");

            withDialog(item::doClick, dialog -> dialog.dispose());
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
