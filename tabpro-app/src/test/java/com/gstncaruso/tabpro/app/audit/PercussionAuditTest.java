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

/**
 * Manual, "Percussion" (linea 1670 del texto extraido): Ver > Asistente de percusion, sin atajo.
 * Solo sirve parado en una pista de percusion; en cualquier otra, el manual describe un aviso en
 * vez del asistente. Los dos caminos reales abren su propio dialogo (los dos son, por adentro,
 * un JOptionPane): se comprueba que cada uno abre alguno, sin repetir aca el contenido del
 * asistente, que ya tiene su propia clase (PercussionAssistant, con sus tests unitarios).
 */
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class PercussionAuditTest {

    @Test
    void elAsistenteDePercusionAbreUnDialogoRealParadoEnUnaPistaDePercusion() throws Exception {
        Editor editor = blankEditor();
        editor.addTrack(Track.percussion("Batería"));
        editor.selectTrack(1);
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Asistente de percusión");
            assertNotNull(item, "no encontre 'Asistente de percusión' en el menu real");

            withDialog(item::doClick, dialog -> dialog.dispose());
        } finally {
            AuditSupport.dispose(frame);
        }
    }

    @Test
    void elAsistenteDePercusionAvisaEnVezDeAbrirseEnUnaPistaQueNoEsDePercusion() throws Exception {
        Editor editor = blankEditor();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Asistente de percusión");
            assertNotNull(item, "no encontre 'Asistente de percusión' en el menu real");

            withDialog(item::doClick, dialog -> dialog.dispose());
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
