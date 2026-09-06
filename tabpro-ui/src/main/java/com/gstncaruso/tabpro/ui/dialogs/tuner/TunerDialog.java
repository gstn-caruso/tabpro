package com.gstncaruso.tabpro.ui.dialogs.tuner;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.notation.PitchName;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/** La ventana del afinador: el afinador MIDI y el afinador digital de la pista activa. */
public final class TunerDialog {

    private TunerDialog() {
    }

    public static void show(Component parent, Editor editor, Player player) {
        show(parent, editor, player, Ports.Microphone.NONE);
    }

    public static void show(Component parent, Editor editor, Player player, Ports.Microphone microphone) {
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
        digitalTab.add(listen(microphone, digitalTuner), BorderLayout.SOUTH);

        JPanel midiTab = new JPanel(new BorderLayout());
        DialogStyle.padded(midiTab);
        midiTab.add(midiTuner, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Afinador MIDI", midiTab);
        tabs.addTab("Afinador digital", digitalTab);

        DialogShell.show(parent, "Afinador", tabs);
        midiTuner.stopAllLoops();
        microphone.stopListening();
    }

    /**
     * Conecta la aguja con la entrada de audio. Si la maquina no tiene entrada,
     * se dice, en vez de dejar una aguja que no se mueve nunca.
     */
    private static Component listen(Ports.Microphone microphone, DigitalTunerPanel needle) {
        JLabel state = new JLabel(microphone.isAvailable()
                ? "Tocá una cuerda al aire."
                : "Esta máquina no tiene entrada de audio.");
        if (!microphone.isAvailable()) {
            return state;
        }
        microphone.startListening(heard -> SwingUtilities.invokeLater(() -> {
            if (!heard.audible()) {
                state.setText("Tocá una cuerda al aire.");
                return;
            }
            needle.setDeviationCents(centsBetween(heard.frequencyHz(), needle.target()));
            state.setText(String.format(java.util.Locale.ROOT, "%.1f Hz", heard.frequencyHz()));
        }));
        return state;
    }

    /** Cuantas centesimas de semitono separan lo que suena de la cuerda elegida. */
    private static int centsBetween(double frequencyHz, com.gstncaruso.tabpro.core.model.Pitch target) {
        double midiNumber = 69 + 12 * Math.log(frequencyHz / 440.0) / Math.log(2);
        return (int) Math.round((midiNumber - target.midiNumber()) * 100);
    }
}
