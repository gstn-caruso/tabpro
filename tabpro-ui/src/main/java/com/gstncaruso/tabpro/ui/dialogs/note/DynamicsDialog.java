package com.gstncaruso.tabpro.ui.dialogs.note;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.dialogs.style.LabeledListCellRenderer;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

public final class DynamicsDialog {

    private DynamicsDialog() {
    }

    public static void show(Component parent, Editor editor) {
        Dynamic current = editor.currentNote().map(note -> note.effects().dynamic()).orElse(Dynamic.defaultDynamic());
        Fields fields = buildFields(current);

        if (!DialogShell.ask(parent, "Dinámica", fields.form())) {
            return;
        }
        Dynamic chosen = (Dynamic) fields.dynamics().getSelectedItem();
        if (fields.wholeChord().isSelected()) {
            editor.setChordDynamic(chosen);
        } else {
            editor.setDynamic(chosen);
        }
    }

    static Fields buildFields(Dynamic current) {
        JComboBox<Dynamic> dynamics = new JComboBox<>(Dynamic.values());
        dynamics.setRenderer(new LabeledListCellRenderer());
        dynamics.setSelectedItem(current);
        JCheckBox wholeChord = new JCheckBox("Aplicar a todo el acorde");

        FormPanel form = new FormPanel()
                .addRow("Dinámica", dynamics)
                .addRow("", wholeChord);

        return new Fields(form, dynamics, wholeChord);
    }

    record Fields(FormPanel form, JComboBox<Dynamic> dynamics, JCheckBox wholeChord) {
    }
}
