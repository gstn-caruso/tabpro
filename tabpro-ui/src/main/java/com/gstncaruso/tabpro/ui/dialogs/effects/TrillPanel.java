package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.Trill;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.dialogs.style.LabeledListCellRenderer;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class TrillPanel extends FormPanel {

    private final JSpinner fret = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
    private final JComboBox<NoteValue> speed = new JComboBox<>(NoteValue.values());

    public TrillPanel(Trill initial) {
        speed.setRenderer(new LabeledListCellRenderer());
        addRow(Texts.get("edit_dialogs.TrillPanel.fret"), fret);
        addRow(Texts.get("edit_dialogs.shared.speed"), speed);
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
