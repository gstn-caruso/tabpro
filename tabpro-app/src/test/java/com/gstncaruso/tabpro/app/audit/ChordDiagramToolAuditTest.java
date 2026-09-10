package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithANote;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findMenuItem;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findRadioButton;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import javax.swing.JMenuItem;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class ChordDiagramToolAuditTest {

    @Test
    void acordePorElAtajoAAbreElDialogoRealYElRadioDePosicionesLlegaAlBeat() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Acorde…");
            assertNotNull(item, "no encontre 'Acorde…' en el menu real");
            assertEquals(javax.swing.KeyStroke.getKeyStroke("A"), item.getAccelerator());

            withDialog(item::doClick, dialog -> {
                var simple = findRadioButton(dialog, "Simple");
                assertNotNull(simple, "no encontre el radio 'Simple' en el dialogo real");
                simple.doClick();

                AuditSupport.findButton(dialog, "Aceptar").doClick();
            });

            assertTrue(editor.currentBeat().effects().chord().isPresent(),
                    "el acorde armado con el radio 'Simple' del dialogo real tiene que llegar al beat");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
