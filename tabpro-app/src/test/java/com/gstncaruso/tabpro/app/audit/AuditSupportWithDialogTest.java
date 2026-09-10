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

@Tag("integration")
@ResourceLock(AuditSupport.SWING_LOCK)
class AuditSupportWithDialogTest {

    @Test
    void aCallbackThatThrowsDoesNotLeaveTheDialogOpenAndRethrowsTheRealError() {
        IllegalStateException rethrown = assertThrows(IllegalStateException.class, () -> withDialog(
                AuditSupportWithDialogTest::openRealModalDialog,
                dialog -> {
                    throw new IllegalStateException("the button I am looking for does not exist in this dialog");
                }));

        assertEquals("the button I am looking for does not exist in this dialog", rethrown.getMessage());
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
        JDialog dialog = new JDialog((java.awt.Frame) null, "test dialog", true);
        dialog.add(new JLabel("content"));
        dialog.pack();
        dialog.setVisible(true);
    }
}
