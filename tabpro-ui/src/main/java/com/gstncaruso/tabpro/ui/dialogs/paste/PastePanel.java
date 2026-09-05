package com.gstncaruso.tabpro.ui.dialogs.paste;

import com.gstncaruso.tabpro.core.editing.PasteOptions;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/** Como pegar lo copiado: insertando o reemplazando, y cuantas veces. */
public final class PastePanel extends FormPanel {

    private final JRadioButton inserting = new JRadioButton("Insertar", true);
    private final JRadioButton replacing = new JRadioButton("Reemplazar");
    private final JSpinner repetitions = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));

    public PastePanel() {
        ButtonGroup group = new ButtonGroup();
        group.add(inserting);
        group.add(replacing);

        javax.swing.JPanel modeRow = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, DialogStyle.GAP_XS));
        modeRow.add(inserting);
        modeRow.add(replacing);

        addFullWidthRow(modeRow);
        addRow("Repeticiones", repetitions);
    }

    public void selectReplacing() {
        replacing.setSelected(true);
    }

    public void setRepetitions(int repetitions) {
        this.repetitions.setValue(repetitions);
    }

    public PasteOptions toPasteOptions() {
        return new PasteOptions(inserting.isSelected(), (Integer) repetitions.getValue());
    }
}
