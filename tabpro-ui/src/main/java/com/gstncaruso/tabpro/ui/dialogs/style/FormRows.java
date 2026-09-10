package com.gstncaruso.tabpro.ui.dialogs.style;

import com.gstncaruso.tabpro.ui.a11y.MnemonicAssigner;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

final class FormRows {

    private final JPanel target;
    private final MnemonicAssigner labelMnemonics;
    private int row = 0;

    FormRows(JPanel target, MnemonicAssigner labelMnemonics) {
        this.target = target;
        this.labelMnemonics = labelMnemonics;
        target.setLayout(new GridBagLayout());
    }

    void addRow(String label, JComponent field) {
        addLabeledRow(label, field, field);
    }

    void addRow(String label, JComponent field, JComponent trailing) {
        JPanel withTrailing = new JPanel(new BorderLayout(DialogStyle.GAP_S, 0));
        withTrailing.setOpaque(false);
        withTrailing.add(field, BorderLayout.CENTER);
        withTrailing.add(trailing, BorderLayout.EAST);
        addLabeledRow(label, withTrailing, field);
    }

    private void addLabeledRow(String label, JComponent layoutComponent, JComponent labeledField) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(DialogStyle.GAP_XS, 0, DialogStyle.GAP_XS, DialogStyle.GAP_S);
        JLabel labelComponent = new JLabel(label);
        labelComponent.setLabelFor(labeledField);
        labelMnemonics.applyTo(labelComponent);
        target.add(labelComponent, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(DialogStyle.GAP_XS, 0, DialogStyle.GAP_XS, 0);
        target.add(layoutComponent, fieldConstraints);

        row++;
    }

    void addFullWidthRow(JComponent component) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(DialogStyle.GAP_XS, 0, DialogStyle.GAP_XS, 0);
        target.add(component, constraints);
        row++;
    }
}
