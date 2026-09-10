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

class FormPanelTest {

    @Test
    void aSimpleRowLabelsTheField() {
        FormPanel panel = new FormPanel();
        JTextField field = new JTextField();

        panel.addRow("Título", field);

        assertEquals("Título", field.getAccessibleContext().getAccessibleName());
    }

    @Test
    void aRowWithTrailingLabelsTheFieldAndNotThePanelWrappingIt() {
        FormPanel panel = new FormPanel();
        JTextField field = new JTextField();
        JButton trailing = new JButton("Escuchar");

        panel.addRow("Nombre", field, trailing);

        assertEquals("Nombre", field.getAccessibleContext().getAccessibleName());
    }

    @Test
    void aSimpleRowGivesItsLabelAMnemonic() {
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
    void aSectionHasATitledBorderWithTheGivenText() {
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
    void theInnerPaddingOfASectionIsTheOneMeasuredInGuitarPro5ScaledToTabpro() {
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
    void eachSectionKeepsTheRowsThatFollowUntilTheNextSection() {
        FormPanel panel = new FormPanel();
        JTextField outsideAnySection = new JTextField();
        JTextField inNotation = new JTextField();
        JTextField inStyle = new JTextField();

        panel.addRow("Nombre", outsideAnySection);
        panel.addSection("Notation");
        panel.addRow("Tablatura", inNotation);
        panel.addSection("Style");
        panel.addRow("Sangria", inStyle);

        JPanel notation = sectionTitled(panel, "Notation");
        JPanel style = sectionTitled(panel, "Style");
        assertFalse(SwingUtilities.isDescendingFrom(outsideAnySection, notation));
        assertFalse(SwingUtilities.isDescendingFrom(outsideAnySection, style));
        assertTrue(SwingUtilities.isDescendingFrom(inNotation, notation));
        assertFalse(SwingUtilities.isDescendingFrom(inNotation, style));
        assertTrue(SwingUtilities.isDescendingFrom(inStyle, style));
        assertFalse(SwingUtilities.isDescendingFrom(inStyle, notation));
    }

    @Test
    void twoConsecutiveSectionsWithoutRowsEndUpAsSeparateBoxes() {
        FormPanel panel = new FormPanel();

        panel.addSection("Encabezado");
        panel.addSection("Pie de pagina");

        assertEquals("Encabezado", sectionTitled(panel, "Encabezado").getAccessibleContext().getAccessibleName());
        assertEquals("Pie de pagina", sectionTitled(panel, "Pie de pagina").getAccessibleContext().getAccessibleName());
    }

    @Test
    void twoDetachedSectionsPlacedSideBySideEndUpAsDescendantsOfTheForm() {
        FormPanel panel = new FormPanel();
        FormPanel.Section left = panel.newDetachedSection("Puerto 1");
        FormPanel.Section right = panel.newDetachedSection("Puerto 2");

        panel.addSideBySide(left, right);

        assertTrue(SwingUtilities.isDescendingFrom(left.asComponent(), panel));
        assertTrue(SwingUtilities.isDescendingFrom(right.asComponent(), panel));
    }

    @Test
    void twoDetachedSectionsCompareMnemonicsToAvoidRepeatingALetter() {
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
