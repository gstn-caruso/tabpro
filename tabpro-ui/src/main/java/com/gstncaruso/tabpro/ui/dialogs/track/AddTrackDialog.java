package com.gstncaruso.tabpro.ui.dialogs.track;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.dialogs.style.LabeledListCellRenderer;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public final class AddTrackDialog {

    private AddTrackDialog() {
    }

    public static void show(Component parent, Editor editor) {
        Fields fields = buildFields(editor.score().trackCount() + 1);

        if (!DialogShell.ask(parent, Texts.get("score_dialogs.shared.addTrack"), fields.form())) {
            return;
        }
        Track track = fields.percussion().isSelected()
                ? Track.percussion(fields.name().getText())
                : trackWith(fields.name().getText(), (Tuning) fields.tunings().getSelectedItem());
        if (fields.beforeCurrent().isSelected()) {
            editor.addTrackAt(editor.cursor().track(), track);
        } else {
            editor.addTrack(track);
        }
    }

    static Fields buildFields(int nextTrackNumber) {
        JTextField name = new JTextField(Texts.get("score_dialogs.AddTrackDialog.defaultName", nextTrackNumber), 16);
        JRadioButton instrumental = new JRadioButton(Texts.get("score_dialogs.AddTrackDialog.instrumental"), true);
        JRadioButton percussion = new JRadioButton(Texts.get("score_dialogs.AddTrackDialog.percussion"));
        group(instrumental, percussion);

        JComboBox<Tuning> tunings = new JComboBox<>(TuningLibrary.all().toArray(Tuning[]::new));
        tunings.setRenderer(new LabeledListCellRenderer());
        instrumental.addActionListener(event -> tunings.setEnabled(true));
        percussion.addActionListener(event -> tunings.setEnabled(false));

        JRadioButton atTheEnd = new JRadioButton(Texts.get("score_dialogs.AddTrackDialog.atTheEnd"), true);
        JRadioButton beforeCurrent = new JRadioButton(Texts.get("score_dialogs.AddTrackDialog.beforeCurrentTrack"));
        group(atTheEnd, beforeCurrent);

        FormPanel form = new FormPanel()
                .addRow(Texts.get("score_dialogs.shared.name"), name)
                .addRow(Texts.get("score_dialogs.AddTrackDialog.type"), instrumental)
                .addRow("", percussion)
                .addRow(Texts.get("score_dialogs.shared.tuning"), tunings)
                .addRow(Texts.get("score_dialogs.shared.position"), atTheEnd)
                .addRow("", beforeCurrent);

        return new Fields(form, name, percussion, tunings, beforeCurrent);
    }

    record Fields(
            FormPanel form,
            JTextField name,
            JRadioButton percussion,
            JComboBox<Tuning> tunings,
            JRadioButton beforeCurrent) {
    }

    private static Track trackWith(String name, Tuning tuning) {
        Track base = tuning.stringCount() <= 4 ? Track.standardBass(name) : Track.standardGuitar(name);
        return base.withTuning(tuning);
    }

    private static void group(JRadioButton... options) {
        ButtonGroup group = new ButtonGroup();
        for (JRadioButton option : options) {
            group.add(option);
        }
    }
}
