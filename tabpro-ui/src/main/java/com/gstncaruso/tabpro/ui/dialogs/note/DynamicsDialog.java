package com.gstncaruso.tabpro.ui.dialogs.note;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.effects.Dynamic;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

/** La ventana de Nota > Dinamica: de muy suave a muy fuerte, para la nota o el acorde. */
public final class DynamicsDialog {

    private DynamicsDialog() {
    }

    public static void show(Component parent, Editor editor) {
        Dynamic current = editor.currentNote().map(note -> note.effects().dynamic()).orElse(Dynamic.defaultDynamic());
        JComboBox<Dynamic> dynamics = new JComboBox<>(Dynamic.values());
        dynamics.setSelectedItem(current);
        JCheckBox wholeChord = new JCheckBox("Aplicar a todo el acorde");

        FormPanel form = new FormPanel()
                .addRow("Dinámica", dynamics)
                .addRow("", wholeChord);

        if (!DialogShell.ask(parent, "Dinámica", form)) {
            return;
        }
        Dynamic chosen = (Dynamic) dynamics.getSelectedItem();
        if (wholeChord.isSelected()) {
            editor.setChordDynamic(chosen);
        } else {
            editor.setDynamic(chosen);
        }
    }
}
