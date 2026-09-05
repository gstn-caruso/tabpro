package com.gstncaruso.tabpro.ui.dialogs.preferences;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

/** Las preferencias generales: la ventana de Preferencias [F12]. */
public final class PreferencesPanel extends FormPanel {

    private final JComboBox<NoteValue> defaultNoteValue = new JComboBox<>(NoteValue.values());
    private final JCheckBox countIn = new JCheckBox("Cuenta regresiva antes de reproducir");
    private final JCheckBox autoScroll = new JCheckBox("Desplazar la pantalla durante la reproduccion");

    public PreferencesPanel(Preferences initial) {
        addRow("Figura por defecto al insertar", defaultNoteValue);
        addFullWidthRow(countIn);
        addFullWidthRow(autoScroll);
        apply(initial);
    }

    public void apply(Preferences preferences) {
        defaultNoteValue.setSelectedItem(preferences.defaultNoteValue());
        countIn.setSelected(preferences.countIn());
        autoScroll.setSelected(preferences.autoScrollDuringPlayback());
    }

    public Preferences toPreferences() {
        return new Preferences((NoteValue) defaultNoteValue.getSelectedItem(), countIn.isSelected(), autoScroll.isSelected());
    }
}
