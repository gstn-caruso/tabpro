package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.model.effects.HarmonicType;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import javax.swing.JComboBox;

public final class HarmonicPanel extends FormPanel {

    private final JComboBox<HarmonicType> type = new JComboBox<>(HarmonicType.values());

    public HarmonicPanel(HarmonicType initial) {
        type.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "" : value.name() + " (" + value.symbol() + ")"));
        addRow(Texts.get("edit_dialogs.shared.type"), type);
        type.setSelectedItem(initial);
    }

    public HarmonicType toHarmonicType() {
        return (HarmonicType) type.getSelectedItem();
    }
}
