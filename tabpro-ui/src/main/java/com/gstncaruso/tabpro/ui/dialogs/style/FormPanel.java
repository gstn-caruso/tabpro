package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.ui.a11y.MnemonicAssigner;
import com.gstncaruso.tabpro.ui.a11y.MnemonicScope;
import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Un formulario de etiquetas alineadas a la izquierda y campos a la derecha, con
 * el mismo aire en todas las ventanas del manual.
 */
public class FormPanel extends JPanel implements MnemonicScope {

    private final MnemonicAssigner labelMnemonics = new MnemonicAssigner();
    private final FormRows sections;
    private FormRows currentRows;

    public FormPanel() {
        DialogStyle.padded(this);
        sections = new FormRows(this, labelMnemonics);
        currentRows = sections;
    }

    /** Una fila con etiqueta a la izquierda y un campo que ocupa el resto del ancho. */
    public FormPanel addRow(String label, JComponent field) {
        currentRows.addRow(label, field);
        return this;
    }

    /** Una fila con un campo mas un componente al lado, por ejemplo un boton de escuchar. */
    public FormPanel addRow(String label, JComponent field, JComponent trailing) {
        currentRows.addRow(label, field, trailing);
        return this;
    }

    /** Un componente que ocupa las dos columnas, como un area de texto o una lista. */
    public FormPanel addFullWidthRow(JComponent component) {
        currentRows.addFullWidthRow(component);
        return this;
    }

    /** El titulo de un nuevo grupo de campos dentro del mismo formulario. */
    public FormPanel addSection(String title) {
        JPanel section = DialogStyle.section(title);
        sections.addFullWidthRow(section);
        currentRows = new FormRows(section, labelMnemonics);
        return this;
    }

    /**
     * Una seccion que el caller ubica donde quiera (por ejemplo, al lado de otra con
     * {@link #addSideBySide}) en vez de apilarla en el flujo vertical normal. Comparte los
     * mnemonicos del formulario para no repetir letra con el resto de las filas.
     */
    public Section newDetachedSection(String title) {
        return new Section(DialogStyle.section(title));
    }

    /** Dos secciones detached, una al lado de la otra, mitad del ancho cada una. */
    public FormPanel addSideBySide(Section left, Section right) {
        JPanel row = new JPanel(new GridLayout(1, 2, DialogStyle.GAP_M, 0));
        row.setOpaque(false);
        row.add(left.panel);
        row.add(right.panel);
        return addFullWidthRow(row);
    }

    /** Una seccion con borde titulado propio, separada del flujo vertical del formulario. */
    public final class Section {

        private final JPanel panel;
        private final FormRows rows;

        private Section(JPanel panel) {
            this.panel = panel;
            this.rows = new FormRows(panel, labelMnemonics);
        }

        public Section addRow(String label, JComponent field) {
            rows.addRow(label, field);
            return this;
        }

        public Section addRow(String label, JComponent field, JComponent trailing) {
            rows.addRow(label, field, trailing);
            return this;
        }

        public JPanel asComponent() {
            return panel;
        }
    }
}
