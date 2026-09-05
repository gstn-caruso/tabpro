package com.gstncaruso.tabpro.ui.dialogs.measure;

import com.gstncaruso.tabpro.core.model.TimeSignature;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/** El numerador y el denominador de la medida del compas. */
public final class TimeSignaturePanel extends FormPanel {

    private static final Integer[] BEAT_UNITS = {1, 2, 4, 8, 16, 32, 64};

    private final JSpinner beats = new JSpinner(new SpinnerNumberModel(4, 1, 32, 1));
    private final JComboBox<Integer> beatUnit = new JComboBox<>(BEAT_UNITS);

    public TimeSignaturePanel(TimeSignature initial) {
        addRow("Pulsos por compas", beats);
        addRow("Figura que vale un pulso", beatUnit);
        apply(initial);
    }

    public void apply(TimeSignature timeSignature) {
        beats.setValue(timeSignature.beats());
        beatUnit.setSelectedItem(timeSignature.beatUnit());
    }

    public TimeSignature toTimeSignature() {
        return new TimeSignature((Integer) beats.getValue(), (Integer) beatUnit.getSelectedItem());
    }
}
