package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ButtonBarTest {

    @Test
    void theCancelButtonDefaultsToTheSpanishLabel() {
        ButtonBar bar = ButtonBar.acceptCancel("whatever");

        assertEquals("Cancelar", bar.cancelButton().getText());
    }
}
