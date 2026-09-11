package com.gstncaruso.tabpro.ui.dialogs.preferences;

import com.gstncaruso.tabpro.core.model.NoteValue;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.dialogs.style.LabeledListCellRenderer;
import com.gstncaruso.tabpro.ui.i18n.Language;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class PreferencesPanel extends FormPanel {

    private final JComboBox<NoteValue> defaultNoteValue = new JComboBox<>(NoteValue.values());
    private final JCheckBox autoScroll = new JCheckBox(Texts.get("score_dialogs.PreferencesPanel.autoScroll"));
    private final JCheckBox showBassInChordName =
            new JCheckBox(Texts.get("score_dialogs.PreferencesPanel.showBassInChordName"));
    private final JCheckBox undoEnabled = new JCheckBox(Texts.get("score_dialogs.PreferencesPanel.undoEnabled"));
    private final JCheckBox forceMultitrack =
            new JCheckBox(Texts.get("score_dialogs.PreferencesPanel.forceMultitrack"));
    private final JSpinner autosaveEvery = new JSpinner(new SpinnerNumberModel(20, 0, 1000, 1));
    private final JSpinner interfaceFontSize = new JSpinner(new SpinnerNumberModel(12, 10, 20, 1));
    private final JCheckBox highContrast = new JCheckBox(Texts.get("score_dialogs.PreferencesPanel.highContrast"));
    private final JCheckBox disableAnimations = new JCheckBox(Texts.get("score_dialogs.PreferencesPanel.disableAnimations"));
    private Language interfaceLanguage;

    public PreferencesPanel(Preferences initial) {
        defaultNoteValue.setRenderer(new LabeledListCellRenderer());
        addRow(Texts.get("score_dialogs.PreferencesPanel.defaultInsertNoteValue"), defaultNoteValue);
        addFullWidthRow(autoScroll);
        addFullWidthRow(showBassInChordName);
        addFullWidthRow(undoEnabled);
        addFullWidthRow(forceMultitrack);
        addRow(Texts.get("score_dialogs.PreferencesPanel.autosaveEvery"), autosaveEvery);
        addSection(Texts.get("score_dialogs.PreferencesPanel.accessibility"));
        addRow(Texts.get("score_dialogs.PreferencesPanel.interfaceFontSize"), interfaceFontSize);
        addFullWidthRow(highContrast);
        addFullWidthRow(disableAnimations);
        apply(initial);
    }

    public void apply(Preferences preferences) {
        defaultNoteValue.setSelectedItem(preferences.defaultNoteValue());
        autoScroll.setSelected(preferences.autoScrollDuringPlayback());
        showBassInChordName.setSelected(preferences.showBassInChordName());
        undoEnabled.setSelected(preferences.undoEnabled());
        autosaveEvery.setValue(preferences.autosaveEvery());
        forceMultitrack.setSelected(preferences.forceMultitrackInHorizontalMode());
        interfaceFontSize.setValue(preferences.interfaceFontSize());
        highContrast.setSelected(preferences.highContrastEnabled());
        disableAnimations.setSelected(preferences.animationsDisabled());
        interfaceLanguage = preferences.interfaceLanguage();
    }

    public Preferences toPreferences() {
        return new Preferences(
                (NoteValue) defaultNoteValue.getSelectedItem(),
                autoScroll.isSelected(),
                showBassInChordName.isSelected(),
                undoEnabled.isSelected(),
                (Integer) autosaveEvery.getValue(),
                forceMultitrack.isSelected(),
                (Integer) interfaceFontSize.getValue(),
                highContrast.isSelected(),
                disableAnimations.isSelected(),
                interfaceLanguage);
    }
}
