package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

/**
 * Cada fila del formulario etiqueta su campo con JLabel#setLabelFor, para que el nombre
 * accesible del campo salga solo del texto de la etiqueta (Swing lo deriva via
 * LABELED_BY_PROPERTY).
 */
class FormPanelTest {

    @Test
    void unaFilaSimpleEtiquetaElCampo() {
        FormPanel panel = new FormPanel();
        JTextField field = new JTextField();

        panel.addRow("Título", field);

        assertEquals("Título", field.getAccessibleContext().getAccessibleName());
    }

    @Test
    void unaFilaConTrailingEtiquetaElCampoYNoElPanelQueLoEnvuelve() {
        FormPanel panel = new FormPanel();
        JTextField field = new JTextField();
        JButton trailing = new JButton("Escuchar");

        panel.addRow("Nombre", field, trailing);

        assertEquals("Nombre", field.getAccessibleContext().getAccessibleName());
    }

    @Test
    void unaFilaSimpleLeDaMnemonicoASuEtiqueta() {
        FormPanel panel = new FormPanel();
        JTextField field = new JTextField();

        panel.addRow("Título", field);

        JLabel label = labelFor(panel, field);
        assertEquals(KeyEvent.VK_T, label.getDisplayedMnemonic());
    }

    private JLabel labelFor(FormPanel panel, JTextField field) {
        for (java.awt.Component component : panel.getComponents()) {
            if (component instanceof JLabel label && label.getLabelFor() == field) {
                return label;
            }
        }
        throw new AssertionError("no se encontro una etiqueta para " + field);
    }
}
