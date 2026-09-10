package com.gstncaruso.tabpro.app.audit;

import static com.gstncaruso.tabpro.app.audit.AuditSupport.withDialog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JDialog;
import javax.swing.JLabel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@Tag("integracion")
@ResourceLock(AuditSupport.SWING_LOCK)
class AuditSupportWithDialogTest {

    @Test
    void aCallbackThatThrowsDoesNotLeaveTheDialogOpenAndRethrowsTheRealError() {
        IllegalStateException rethrown = assertThrows(IllegalStateException.class, () -> withDialog(
                AuditSupportWithDialogTest::openRealModalDialog,
                dialog -> {
                    throw new IllegalStateException("el boton que busco no existe en este dialogo");
                }));

        assertEquals("el boton que busco no existe en este dialogo", rethrown.getMessage());
    }

    @Test
    void aDialogThatNeverOpensFailsWithATimeoutInsteadOfHanging() {
        AssertionError error = assertThrows(AssertionError.class, () -> withDialog(() -> { }, dialog -> { }));

        assertTrue(error.getMessage().contains("WINDOW_OPENED"));
    }

    @Test
    void aCallbackThatForgetsToCloseTheDialogFailsInsteadOfHanging() {
        AssertionError error = assertThrows(AssertionError.class, () -> withDialog(
                AuditSupportWithDialogTest::openRealModalDialog, dialog -> { }));

        assertTrue(error.getMessage().contains("dispose"));
    }

    private static void openRealModalDialog() {
        JDialog dialog = new JDialog((java.awt.Frame) null, "dialogo de prueba", true);
        dialog.add(new JLabel("contenido"));
        dialog.pack();
        dialog.setVisible(true);
    }
}
