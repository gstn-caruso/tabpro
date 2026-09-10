package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.swing.JDialog;
import javax.swing.JLabel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class AuditSupportWithDialogTest {

    @Test
    void elCallbackQueTiraNoDejaElDialogoAbiertoYRelanzaElErrorReal() {
        IllegalStateException relanzada = assertThrows(IllegalStateException.class, () -> withDialog(
                AuditSupportWithDialogTest::openRealModalDialog,
                dialog -> {
                    throw new IllegalStateException("el boton que busco no existe en este dialogo");
                }));

        assertEquals("el boton que busco no existe en este dialogo", relanzada.getMessage());
    }

    private static void openRealModalDialog() {
        JDialog dialog = new JDialog((java.awt.Frame) null, "dialogo de prueba", true);
        dialog.add(new JLabel("contenido"));
        dialog.pack();
        dialog.setVisible(true);
    }
}
