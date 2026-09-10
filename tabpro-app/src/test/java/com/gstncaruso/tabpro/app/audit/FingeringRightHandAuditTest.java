package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.awaitDialog;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.awaitFocusOwner;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.dispose;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.editorWithANote;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButton;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findButtonByAccessibleName;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.findComponents;
import static com.gstncaruso.tabpro.app.audit.AuditSupport.newFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.ui.MainFrame;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class FingeringRightHandAuditTest {

    @Test
    void theRightHandButtonOpensTheRealDialogWithFocusOnItsOwnField() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JButton button = findButtonByAccessibleName(frame.getContentPane(), "Digitación (mano derecha)…");
            assertNotNull(button, "no encontre el boton real de digitacion de mano derecha");

            JDialog dialog = awaitDialog(button::doClick, 5000);
            try {
                @SuppressWarnings({"unchecked", "rawtypes"})
                List<JComboBox> combos = findComponents(dialog, JComboBox.class);
                assertEquals(2, combos.size(), "el dialogo real tiene que traer los dos campos de digitacion");

                assertTrue(awaitFocusOwner(combos.get(1), 2000),
                        "el campo de mano derecha tiene que arrancar con el foco real");
            } finally {
                SwingUtilities.invokeAndWait(() -> findButton(dialog, "Cancelar").doClick());
            }
        } finally {
            dispose(frame);
        }
    }
}
