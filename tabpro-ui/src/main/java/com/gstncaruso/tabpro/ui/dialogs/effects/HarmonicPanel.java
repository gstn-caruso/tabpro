package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JComboBox;

/** El armonico: cual de los cinco tipos que reconoce la tablatura. */
public final class HarmonicPanel extends FormPanel {

    private final JComboBox<HarmonicType> type = new JComboBox<>(HarmonicType.values());

    public HarmonicPanel(HarmonicType initial) {
        type.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "" : value.name() + " (" + value.symbol() + ")"));
        addRow("Tipo", type);
        type.setSelectedItem(initial);
    }

    public HarmonicType toHarmonicType() {
        return (HarmonicType) type.getSelectedItem();
    }
}
