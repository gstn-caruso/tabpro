package com.gstncaruso.tabpro.ui.dialogs.measure;

import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.dialogs.style.Labels;
import javax.swing.JComboBox;

public final class TripletFeelPanel extends FormPanel {

    private final JComboBox<TripletFeel> tripletFeel = new JComboBox<>(TripletFeel.values());

    public TripletFeelPanel(TripletFeel initial) {
        tripletFeel.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "" : Labels.of(value)));
        addRow("Triplet feel", tripletFeel);
        apply(initial);
    }

    public void apply(TripletFeel feel) {
        tripletFeel.setSelectedItem(feel);
    }

    public TripletFeel toTripletFeel() {
        return (TripletFeel) tripletFeel.getSelectedItem();
    }
}
