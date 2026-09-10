package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.ui.a11y.MnemonicAssigner;
import com.gstncaruso.tabpro.ui.a11y.MnemonicScope;
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
        sections.addFullWidthRow(DialogStyle.sectionLabel(title));
        return this;
    }
}
