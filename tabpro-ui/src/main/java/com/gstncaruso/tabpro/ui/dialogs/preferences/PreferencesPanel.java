package com.gstncaruso.tabpro.ui.dialogs.preferences;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/** Las preferencias generales: la ventana de Preferencias [F12]. */
public final class PreferencesPanel extends FormPanel {

    private final JComboBox<NoteValue> defaultNoteValue = new JComboBox<>(NoteValue.values());
    private final JCheckBox countIn = new JCheckBox("Cuenta regresiva antes de reproducir");
    private final JCheckBox autoScroll = new JCheckBox("Desplazar la pantalla durante la reproduccion");
    private final JCheckBox showBassInChordName =
            new JCheckBox("Indicar el bajo en el nombre del acorde cuando es distinto de la fundamental");
    private final JCheckBox undoEnabled = new JCheckBox("Deshacer y rehacer");
    private final JSpinner autosaveEvery = new JSpinner(new SpinnerNumberModel(20, 0, 1000, 1));

    public PreferencesPanel(Preferences initial) {
        addRow("Figura por defecto al insertar", defaultNoteValue);
        addFullWidthRow(countIn);
        addFullWidthRow(autoScroll);
        addFullWidthRow(showBassInChordName);
        addFullWidthRow(undoEnabled);
        addRow("Guardado automatico cada N acciones", autosaveEvery);
        apply(initial);
    }

    public void apply(Preferences preferences) {
        defaultNoteValue.setSelectedItem(preferences.defaultNoteValue());
        countIn.setSelected(preferences.countIn());
        autoScroll.setSelected(preferences.autoScrollDuringPlayback());
        showBassInChordName.setSelected(preferences.showBassInChordName());
        undoEnabled.setSelected(preferences.undoEnabled());
        autosaveEvery.setValue(preferences.autosaveEvery());
    }

    public Preferences toPreferences() {
        return new Preferences(
                (NoteValue) defaultNoteValue.getSelectedItem(),
                countIn.isSelected(),
                autoScroll.isSelected(),
                showBassInChordName.isSelected(),
                undoEnabled.isSelected(),
                (Integer) autosaveEvery.getValue());
    }
}
