package com.gstncaruso.tabpro.ui.sound;

import com.gstncaruso.tabpro.core.editing.Editor;
import com.gstncaruso.tabpro.core.playback.LoopRange;
import com.gstncaruso.tabpro.core.playback.RelativeTempo;
import com.gstncaruso.tabpro.core.playback.SpeedTrainer;
import com.gstncaruso.tabpro.ui.i18n.Texts;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public final class LoopDialog {

    private LoopDialog() {
    }

    public record Loop(LoopRange range, Optional<SpeedTrainer> trainer) {
    }

    public static Optional<Loop> ask(Component parent, Editor editor, RelativeTempo relativeTempo) {
        int lastMeasure = editor.currentTrack().measureCount();
        int fromDefault = editor.selection().map(selection -> selection.fromMeasure() + 1)
                .orElse(editor.cursor().measure() + 1);
        int toDefault = editor.selection().map(selection -> selection.toMeasure() + 1).orElse(lastMeasure);

        JSpinner from = new JSpinner(new SpinnerNumberModel(fromDefault, 1, lastMeasure, 1));
        JSpinner to = new JSpinner(new SpinnerNumberModel(toDefault, 1, lastMeasure, 1));
        JRadioButton simple = new JRadioButton(Texts.get("views.LoopDialog.simpleLoop"), true);
        JRadioButton trainer = new JRadioButton(Texts.get("views.LoopDialog.speedTrainer"));
        ButtonGroup mode = new ButtonGroup();
        mode.add(simple);
        mode.add(trainer);

        int tempo = relativeTempo.apply(editor.score().tempo());
        JSpinner startTempo = new JSpinner(new SpinnerNumberModel(tempo, 20, 400, 1));
        JSpinner endTempo = new JSpinner(new SpinnerNumberModel(Math.min(400, tempo + 40), 20, 400, 1));
        JSpinner increment = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel(Texts.get("views.LoopDialog.fromBar")));
        panel.add(from);
        panel.add(new JLabel(Texts.get("views.LoopDialog.toBar")));
        panel.add(to);
        panel.add(simple);
        panel.add(new JLabel(" "));
        panel.add(trainer);
        panel.add(new JLabel(" "));
        panel.add(new JLabel(Texts.get("views.LoopDialog.initialTempo")));
        panel.add(startTempo);
        panel.add(new JLabel(Texts.get("views.LoopDialog.finalTempo")));
        panel.add(endTempo);
        panel.add(new JLabel(Texts.get("views.LoopDialog.increasePerLoop")));
        panel.add(increment);

        int answer = JOptionPane.showConfirmDialog(
                parent, panel, Texts.get("views.LoopDialog.title"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }
        LoopRange range = new LoopRange(value(from) - 1, value(to) - 1);
        Optional<SpeedTrainer> speedTrainer = trainer.isSelected()
                ? Optional.of(new SpeedTrainer(value(startTempo), value(endTempo), value(increment)))
                : Optional.empty();
        return Optional.of(new Loop(range, speedTrainer));
    }

    private static int value(JSpinner spinner) {
        return (Integer) spinner.getValue();
    }
}
