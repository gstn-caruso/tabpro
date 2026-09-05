package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/** El trino: con que otro traste alterna y a que velocidad. */
public final class TrillPanel extends FormPanel {

    private final JSpinner fret = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
    private final JComboBox<NoteValue> speed = new JComboBox<>(NoteValue.values());

    public TrillPanel(Trill initial) {
        addRow("Traste de la segunda nota", fret);
        addRow("Velocidad", speed);
        apply(initial);
    }

    public void apply(Trill trill) {
        fret.setValue(trill.fret());
        speed.setSelectedItem(trill.speed());
    }

    public Trill toTrill() {
        return new Trill((Integer) fret.getValue(), (NoteValue) speed.getSelectedItem());
    }
}
