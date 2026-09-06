package com.gstncaruso.tabpro.ui.dialogs.track;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.model.TuningLibrary;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.FormPanel;
import java.awt.Component;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
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
        JTextField name = new JTextField("Pista " + (editor.score().trackCount() + 1), 16);
        JRadioButton instrumental = new JRadioButton("Instrumental", true);
        JRadioButton percussion = new JRadioButton("Percusión");
        group(instrumental, percussion);

        JComboBox<Tuning> tunings = new JComboBox<>(TuningLibrary.all().toArray(Tuning[]::new));
        tunings.setRenderer(tuningNames());
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

        if (!DialogShell.ask(parent, "Agregar una pista", form)) {
            return;
        }
        Track track = percussion.isSelected()
                ? Track.percussion(name.getText())
                : trackWith(name.getText(), (Tuning) tunings.getSelectedItem());
        if (beforeCurrent.isSelected()) {
            editor.addTrackAt(editor.cursor().track(), track);
        } else {
            editor.addTrack(track);
        }
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

    private static DefaultListCellRenderer tuningNames() {
        return new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean selected, boolean focused) {
                super.getListCellRendererComponent(list, value, index, selected, focused);
                if (value instanceof Tuning tuning) {
                    setText(tuning.name());
                }
                return this;
            }
        };
    }
}
