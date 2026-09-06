package com.gstncaruso.tabpro.ui.dialogs.measure;

import com.gstncaruso.tabpro.core.model.bars.MeasureAttributes;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;

/** En que vueltas de la repeticion se toca este compas. */
public final class AlternateEndingsPanel extends FormPanel {

    private final List<JCheckBox> passes = new ArrayList<>();

    public AlternateEndingsPanel(List<Integer> initial) {
        javax.swing.JPanel row = new javax.swing.JPanel(new java.awt.GridLayout(1, 0, DialogStyle.GAP_S, 0));
        row.setOpaque(false);
        for (int pass = 1; pass <= MeasureAttributes.MAX_ALTERNATE_ENDINGS; pass++) {
            JCheckBox box = new JCheckBox(String.valueOf(pass));
            box.setSelected(initial.contains(pass));
            passes.add(box);
            row.add(box);
        }
        addRow("Finales alternativos", row);
    }

    /** Marca o desmarca una vuelta puntual, para poblar el formulario o para probarlo. */
    public void setChecked(int pass, boolean checked) {
        passes.get(pass - 1).setSelected(checked);
    }

    public List<Integer> toAlternateEndings() {
        List<Integer> selected = new ArrayList<>();
        for (int index = 0; index < passes.size(); index++) {
            if (passes.get(index).isSelected()) {
                selected.add(index + 1);
            }
        }
        return List.copyOf(selected);
    }
}
