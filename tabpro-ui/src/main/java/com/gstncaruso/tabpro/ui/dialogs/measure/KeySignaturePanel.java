package com.gstncaruso.tabpro.ui.dialogs.measure;

import com.gstncaruso.tabpro.core.model.bars.KeySignature;
import com.gstncaruso.tabpro.core.model.bars.Mode;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;

/** La tonalidad del compas: cuantas alteraciones lleva y si es mayor o menor. */
public final class KeySignaturePanel extends FormPanel {

    private final JComboBox<KeySignature> keySignature = new JComboBox<>(everyKeySignature());

    public KeySignaturePanel(KeySignature initial) {
        keySignature.setRenderer((list, value, index, isSelected, hasFocus) ->
                new javax.swing.JLabel(value == null ? "" : value.name() + " (" + value.mode().label() + ")"));
        addRow("Armadura", keySignature);
        apply(initial);
    }

    private static KeySignature[] everyKeySignature() {
        List<KeySignature> all = new ArrayList<>();
        for (Mode mode : Mode.values()) {
            for (int accidentals = -7; accidentals <= 7; accidentals++) {
                all.add(new KeySignature(accidentals, mode));
            }
        }
        return all.toArray(new KeySignature[0]);
    }

    public void apply(KeySignature signature) {
        keySignature.setSelectedItem(signature);
    }

    public KeySignature toKeySignature() {
        return (KeySignature) keySignature.getSelectedItem();
    }
}
