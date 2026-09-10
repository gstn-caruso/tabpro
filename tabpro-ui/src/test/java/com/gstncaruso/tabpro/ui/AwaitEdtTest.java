package com.gstncaruso.tabpro.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class AwaitEdtTest {

    @Test
    void waitsUntilTheEdtProcessesWhatWasAlreadyQueued() throws Exception {
        boolean[] ran = {false};
        SwingUtilities.invokeLater(() -> ran[0] = true);

        AwaitEdt.flush();

        assertTrue(ran[0], "flush tiene que esperar a que el EDT procese lo que ya estaba encolado");
    }
}
