package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.ui.a11y.MnemonicAssigner;
import com.gstncaruso.tabpro.ui.a11y.MnemonicScope;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Un formulario de etiquetas alineadas a la izquierda y campos a la derecha, con
 * el mismo aire en todas las ventanas del manual.
 */
public class FormPanel extends JPanel implements MnemonicScope {

    private final GridBagLayout layout = new GridBagLayout();
    private final MnemonicAssigner labelMnemonics = new MnemonicAssigner();
    private int row = 0;

    public FormPanel() {
        setLayout(layout);
        DialogStyle.padded(this);
    }

    /** Una fila con etiqueta a la izquierda y un campo que ocupa el resto del ancho. */
    public FormPanel addRow(String label, JComponent field) {
        return addLabeledRow(label, field, field);
    }

    /** Una fila con un campo mas un componente al lado, por ejemplo un boton de escuchar. */
    public FormPanel addRow(String label, JComponent field, JComponent trailing) {
        JPanel withTrailing = new JPanel(new java.awt.BorderLayout(DialogStyle.GAP_S, 0));
        withTrailing.setOpaque(false);
        withTrailing.add(field, java.awt.BorderLayout.CENTER);
        withTrailing.add(trailing, java.awt.BorderLayout.EAST);
        return addLabeledRow(label, withTrailing, field);
    }

    /** La etiqueta describe {@code labeledField}, aunque en la fila se ubique {@code layout}. */
    private FormPanel addLabeledRow(String label, JComponent layout, JComponent labeledField) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(DialogStyle.GAP_XS, 0, DialogStyle.GAP_XS, DialogStyle.GAP_S);
        JLabel labelComponent = new JLabel(label);
        labelComponent.setLabelFor(labeledField);
        labelMnemonics.applyTo(labelComponent);
        add(labelComponent, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(DialogStyle.GAP_XS, 0, DialogStyle.GAP_XS, 0);
        add(layout, fieldConstraints);

        row++;
        return this;
    }

    /** Un componente que ocupa las dos columnas, como un area de texto o una lista. */
    public FormPanel addFullWidthRow(JComponent component) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(DialogStyle.GAP_XS, 0, DialogStyle.GAP_XS, 0);
        add(component, constraints);
        row++;
        return this;
    }

    /** El titulo de un nuevo grupo de campos dentro del mismo formulario. */
    public FormPanel addSection(String title) {
        return addFullWidthRow(DialogStyle.sectionLabel(title));
    }
}
