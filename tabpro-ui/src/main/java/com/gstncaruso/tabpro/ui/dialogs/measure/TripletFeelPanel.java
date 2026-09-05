package com.gstncaruso.tabpro.ui.dialogs.measure;

import com.gstncaruso.tabpro.core.model.bars.TripletFeel;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JComboBox;

/** La sincopa que se toca aunque no se escriba, desde este compas en adelante. */
public final class TripletFeelPanel extends FormPanel {

    private final JComboBox<TripletFeel> tripletFeel = new JComboBox<>(TripletFeel.values());

    public TripletFeelPanel(TripletFeel initial) {
        tripletFeel.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "" : value.label()));
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
