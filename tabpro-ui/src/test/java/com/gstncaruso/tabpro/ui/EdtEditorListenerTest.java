package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class EdtEditorListenerTest {

    @Test
    void entregaSincronicamenteCuandoYaEstaEnElEdt() throws Exception {
        boolean[] delivered = {false};

        SwingUtilities.invokeAndWait(() -> EdtEditorListener.onEdt(() -> delivered[0] = true).editorChanged());

        assertTrue(delivered[0], "en el EDT, la entrega tiene que ser en el acto");
    }
}
