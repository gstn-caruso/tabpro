package com.gstncaruso.tabpro.ui.dialogs.track;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import com.gstncaruso.tabpro.ui.dialogs.style.LabeledListCellRenderer;
import java.awt.Component;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * La ventana de Pista > Agregar: que clase de pista, con que afinacion y en que
 * lugar de la lista, como describe "Add Tracks".
 */
public final class AddTrackDialog {

    private AddTrackDialog() {
    }

    public static void show(Component parent, Editor editor) {
        Fields fields = buildFields(editor.score().trackCount() + 1);

        if (!DialogShell.ask(parent, "Agregar una pista", fields.form())) {
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

    /** Arma el formulario y los campos que hay que releer si se acepta; sin abrir ningun dialogo. */
    static Fields buildFields(int nextTrackNumber) {
        JTextField name = new JTextField("Pista " + nextTrackNumber, 16);
        JRadioButton instrumental = new JRadioButton("Instrumental", true);
        JRadioButton percussion = new JRadioButton("Percusión");
        group(instrumental, percussion);

        JComboBox<Tuning> tunings = new JComboBox<>(TuningLibrary.all().toArray(Tuning[]::new));
        tunings.setRenderer(new LabeledListCellRenderer());
        instrumental.addActionListener(event -> tunings.setEnabled(true));
        percussion.addActionListener(event -> tunings.setEnabled(false));

        JRadioButton atTheEnd = new JRadioButton("Al final", true);
        JRadioButton beforeCurrent = new JRadioButton("Antes de la pista actual");
        group(atTheEnd, beforeCurrent);

        FormPanel form = new FormPanel()
                .addRow("Nombre", name)
                .addRow("Tipo", instrumental)
                .addRow("", percussion)
                .addRow("Afinación", tunings)
                .addRow("Posición", atTheEnd)
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

    /** Una guitarra o un bajo, segun cuantas cuerdas tenga la afinacion elegida. */
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
