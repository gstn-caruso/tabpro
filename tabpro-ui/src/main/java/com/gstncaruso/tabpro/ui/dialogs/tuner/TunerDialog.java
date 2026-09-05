package com.gstncaruso.tabpro.ui.dialogs.tuner;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.notation.PitchName;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** La ventana del afinador: el afinador MIDI y el afinador digital de la pista activa. */
public final class TunerDialog {

    private TunerDialog() {
    }

    public static void show(Component parent, Editor editor, Player player) {
        Track track = editor.currentTrack();
        Tuning tuning = track.tuning();

        MidiTunerPanel midiTuner = new MidiTunerPanel(tuning, track.channel().program(), player);

        DigitalTunerPanel digitalTuner = new DigitalTunerPanel(tuning.pitchOfString(1));
        JComboBox<Integer> stringChooser = new JComboBox<>();
        for (int string = 1; string <= tuning.stringCount(); string++) {
            stringChooser.addItem(string);
        }
        stringChooser.setRenderer((list, value, index, isSelected, hasFocus) -> new javax.swing.JLabel(
                value == null ? "" : "Cuerda " + value + " (" + PitchName.of(tuning.pitchOfString(value)).textWithOctave() + ")"));
        stringChooser.addActionListener(event -> digitalTuner.setTarget(tuning.pitchOfString((Integer) stringChooser.getSelectedItem())));

        JPanel digitalTab = new JPanel(new BorderLayout(0, DialogStyle.GAP_S));
        DialogStyle.padded(digitalTab);
        digitalTab.add(stringChooser, BorderLayout.NORTH);
        digitalTab.add(digitalTuner, BorderLayout.CENTER);

        JPanel midiTab = new JPanel(new BorderLayout());
        DialogStyle.padded(midiTab);
        midiTab.add(midiTuner, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Afinador MIDI", midiTab);
        tabs.addTab("Afinador digital", digitalTab);

        DialogShell.show(parent, "Afinador", tabs);
        midiTuner.stopAllLoops();
    }
}
