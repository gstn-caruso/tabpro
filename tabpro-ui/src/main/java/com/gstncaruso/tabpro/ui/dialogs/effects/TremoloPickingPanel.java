package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JComboBox;

/** El tremolo de pua: a que velocidad se repite la nota. */
public final class TremoloPickingPanel extends FormPanel {

    private final JComboBox<NoteValue> speed = new JComboBox<>(NoteValue.values());

    public TremoloPickingPanel(TremoloPicking initial) {
        addRow("Velocidad", speed);
        apply(initial);
    }

    public void apply(TremoloPicking picking) {
        speed.setSelectedItem(picking.speed());
    }

    public TremoloPicking toTremoloPicking() {
        return new TremoloPicking((NoteValue) speed.getSelectedItem());
    }
}
