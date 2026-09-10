package com.gstncaruso.tabpro.ui.dialogs.effects;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.core.model.effects.TremoloPicking;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.dialogs.style.LabeledListCellRenderer;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import javax.swing.JComboBox;

public final class TremoloPickingPanel extends FormPanel {

    private final JComboBox<NoteValue> speed = new JComboBox<>(NoteValue.values());

    public TremoloPickingPanel(TremoloPicking initial) {
        speed.setRenderer(new LabeledListCellRenderer());
        addRow(Texts.get("edit_dialogs.shared.speed"), speed);
        apply(initial);
    }

    public void apply(TremoloPicking picking) {
        speed.setSelectedItem(picking.speed());
    }

    public TremoloPicking toTremoloPicking() {
        return new TremoloPicking((NoteValue) speed.getSelectedItem());
    }
}
