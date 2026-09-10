package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

/**
 * La regla general: un dialogo nunca es mas alto que el area util de la pantalla, y los
 * botones de accion siempre quedan visibles fuera de cualquier scroll. El alto disponible
 * se inyecta como parametro para poder probarlo sin un display real.
 */
class DialogShellTest {

    @Test
    void unContenidoQueYaEntraEnElAltoDisponibleNoSeEnvuelveEnNingunScroll() {
        JPanel content = new JPanel();
        content.setPreferredSize(new Dimension(300, 200));

        JComponent fitted = DialogShell.fitToAvailableHeight(content, 400);

        assertSame(content, fitted);
    }
}
