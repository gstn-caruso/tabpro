package com.gstncaruso.tabpro.ui.dialogs.tuner;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.model.Track;
import com.gstncaruso.tabpro.core.model.Tuning;
import com.gstncaruso.tabpro.core.notation.PitchName;
import com.gstncaruso.tabpro.core.playback.Player;
import com.gstncaruso.tabpro.ui.actions.Ports;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogShell;
import com.gstncaruso.tabpro.ui.dialogs.style.DialogStyle;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public final class TunerDialog {

    private TunerDialog() {
    }

    public static void show(Component parent, Editor editor, Player player) {
        show(parent, editor, player, Ports.Microphone.NONE);
    }

    public static void show(Component parent, Editor editor, Player player, Ports.Microphone microphone) {
        Tabs tabs = buildTabs(editor, player, microphone);
        DialogShell.show(parent, Texts.get("score_dialogs.TunerDialog.title"), tabs.pane());
        tabs.midiTuner().stopAllLoops();
        microphone.stopListening();
    }

    static Tabs buildTabs(Editor editor, Player player, Ports.Microphone microphone) {
        Track track = editor.currentTrack();
        Tuning tuning = track.tuning();

        MidiTunerPanel midiTuner = new MidiTunerPanel(tuning, track.channel().program(), player);

        DigitalTunerPanel digitalTuner = new DigitalTunerPanel(tuning.pitchOfString(1));
        JComboBox<Integer> stringChooser = new JComboBox<>();
        for (int string = 1; string <= tuning.stringCount(); string++) {
            stringChooser.addItem(string);
        }
        stringChooser.setRenderer((list, value, index, isSelected, hasFocus) -> new javax.swing.JLabel(
                value == null
                        ? ""
                        : Texts.get("score_dialogs.shared.string", value) + " ("
                                + PitchName.of(tuning.pitchOfString(value)).textWithOctave() + ")"));
        stringChooser.getAccessibleContext().setAccessibleName(Texts.get("score_dialogs.TunerDialog.stringToTune"));
        stringChooser.setToolTipText(Texts.get("score_dialogs.TunerDialog.stringToTune"));
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
        tabs.addTab(Texts.get("score_dialogs.TunerDialog.midiTunerTab"), midiTab);
        tabs.addTab(Texts.get("score_dialogs.DigitalTunerPanel.title"), digitalTab);
        return new Tabs(tabs, midiTuner);
    }

    record Tabs(JTabbedPane pane, MidiTunerPanel midiTuner) {
    }

    private static Component listen(Ports.Microphone microphone, DigitalTunerPanel needle) {
        JLabel state = new JLabel(microphone.isAvailable()
                ? Texts.get("score_dialogs.TunerDialog.playAnOpenString")
                : Texts.get("score_dialogs.TunerDialog.noAudioInput"));
        if (!microphone.isAvailable()) {
            return state;
        }
        microphone.startListening(heard -> SwingUtilities.invokeLater(() -> {
            if (!heard.audible()) {
                state.setText(Texts.get("score_dialogs.TunerDialog.playAnOpenString"));
                return;
            }
            needle.setDeviationCents(centsBetween(heard.frequencyHz(), needle.target()));
            state.setText(String.format(java.util.Locale.ROOT, "%.1f Hz", heard.frequencyHz()));
        }));
        return state;
    }

    private static int centsBetween(double frequencyHz, com.gstncaruso.tabpro.core.model.Pitch target) {
        double midiNumber = 69 + 12 * Math.log(frequencyHz / 440.0) / Math.log(2);
        return (int) Math.round((midiNumber - target.midiNumber()) * 100);
    }
}
