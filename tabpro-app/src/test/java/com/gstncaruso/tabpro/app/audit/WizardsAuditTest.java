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

/**
 * Manual, "Wizards" (linea 1587 del texto extraido): Transponer, en el menu Herramientas. El
 * boton de aceptar de este dialogo no dice "Aceptar" sino "Transponer" -DialogShell.ask lo
 * permite elegir por parametro-, asi que buscarlo por el texto real importa: si alguien lo
 * llamara "Aceptar" a mano en un test, no lo hubiera encontrado.
 */
@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class WizardsAuditTest {

    @Test
    void transponerPorElMenuCambiaLaPartituraRealSegunElSpinnerDeSemitonos() throws Exception {
        Editor editor = editorWithANote();
        MainFrame frame = newFrame(editor);
        try {
            JMenuItem item = findMenuItem(frame.getJMenuBar(), "Transponer…");
            assertNotNull(item, "no encontre 'Transponer…' en el menu real");

            Score antes = editor.score();

            withDialog(item::doClick, dialog -> {
                JSpinner semitonos = findComponent(dialog, JSpinner.class);
                assertNotNull(semitonos, "no encontre el spinner real de semitonos");
                semitonos.setValue(2);

                findButton(dialog, "Transponer").doClick();
            });

            assertNotEquals(antes, editor.score(),
                    "los 2 semitonos elegidos en el spinner real tienen que transponer la partitura real");
        } finally {
            AuditSupport.dispose(frame);
        }
    }
}
