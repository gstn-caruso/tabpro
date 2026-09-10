package com.gstncaruso.tabpro.ui.dialogs.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
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

    @Test
    void unaSeccionTieneBordeTituladoConElTextoDado() {
        FormPanel panel = new FormPanel();

        panel.addSection("Notation");

        JPanel section = sectionTitled(panel, "Notation");
        assertEquals("Notation", section.getAccessibleContext().getAccessibleName());
    }

    private JPanel sectionTitled(FormPanel panel, String title) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JPanel candidate
                    && title.equals(candidate.getAccessibleContext().getAccessibleName())) {
                return candidate;
            }
        }
        throw new AssertionError("no se encontro una seccion titulada " + title);
    }

    @Test
    void elPaddingInteriorDeUnaSeccionEsElMedidoEnGuitarPro5EscaladoATabpro() {
        FormPanel panel = new FormPanel();

        panel.addSection("Notation");

        Insets padding = innerPaddingOf(sectionTitled(panel, "Notation"));
        int expected = DialogStyle.SECTION_INNER_PADDING;
        assertEquals(new Insets(expected, expected, expected, expected), padding);
    }

    private Insets innerPaddingOf(JPanel section) {
        CompoundBorder border = (CompoundBorder) section.getBorder();
        return border.getInsideBorder().getBorderInsets(section);
    }

    @Test
    void cadaSeccionQuedaConLasFilasQueLeSiguenHastaLaProximaSeccion() {
        FormPanel panel = new FormPanel();
        JTextField fueraDeToda = new JTextField();
        JTextField deNotacion = new JTextField();
        JTextField deEstilo = new JTextField();

        panel.addRow("Nombre", fueraDeToda);
        panel.addSection("Notation");
        panel.addRow("Tablatura", deNotacion);
        panel.addSection("Style");
        panel.addRow("Sangria", deEstilo);

        JPanel notation = sectionTitled(panel, "Notation");
        JPanel style = sectionTitled(panel, "Style");
        assertFalse(SwingUtilities.isDescendingFrom(fueraDeToda, notation));
        assertFalse(SwingUtilities.isDescendingFrom(fueraDeToda, style));
        assertTrue(SwingUtilities.isDescendingFrom(deNotacion, notation));
        assertFalse(SwingUtilities.isDescendingFrom(deNotacion, style));
        assertTrue(SwingUtilities.isDescendingFrom(deEstilo, style));
        assertFalse(SwingUtilities.isDescendingFrom(deEstilo, notation));
    }

    @Test
    void dosSeccionesSeguidasSinFilasQuedanComoCajasSeparadas() {
        FormPanel panel = new FormPanel();

        panel.addSection("Encabezado");
        panel.addSection("Pie de pagina");

        assertEquals("Encabezado", sectionTitled(panel, "Encabezado").getAccessibleContext().getAccessibleName());
        assertEquals("Pie de pagina", sectionTitled(panel, "Pie de pagina").getAccessibleContext().getAccessibleName());
    }

    @Test
    void dosSeccionesDetachedUbicadasLadoALadoQuedanComoDescendientesDelFormulario() {
        FormPanel panel = new FormPanel();
        FormPanel.Section left = panel.newDetachedSection("Puerto 1");
        FormPanel.Section right = panel.newDetachedSection("Puerto 2");

        panel.addSideBySide(left, right);

        assertTrue(SwingUtilities.isDescendingFrom(left.asComponent(), panel));
        assertTrue(SwingUtilities.isDescendingFrom(right.asComponent(), panel));
    }

    @Test
    void dosSeccionesDetachedComparenLosMnemonicosParaNoRepetirLetra() {
        FormPanel panel = new FormPanel();
        FormPanel.Section left = panel.newDetachedSection("Puerto 1");
        JTextField leftField = new JTextField();
        left.addRow("Dispositivo", leftField);
        FormPanel.Section right = panel.newDetachedSection("Puerto 2");
        JTextField rightField = new JTextField();
        right.addRow("Dispositivo", rightField);

        panel.addSideBySide(left, right);

        JLabel leftLabel = labelFor(left.asComponent(), leftField);
        JLabel rightLabel = labelFor(right.asComponent(), rightField);
        assertFalse(leftLabel.getDisplayedMnemonic() == rightLabel.getDisplayedMnemonic()
                && leftLabel.getDisplayedMnemonic() != 0);
    }

    private JLabel labelFor(Container root, JTextField field) {
        for (Component component : ((JPanel) root).getComponents()) {
            if (component instanceof JLabel label && label.getLabelFor() == field) {
                return label;
            }
        }
        throw new AssertionError("no se encontro una etiqueta para " + field + " dentro de " + root);
    }
}
